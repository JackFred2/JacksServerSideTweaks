package red.jackf.jsst.mixins.itemnudge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.jackfredlib.api.base.Ephemeral2;
import red.jackf.jsst.impl.config.JSSTConfig;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Unique
    private static final Ephemeral2<ServerPlayer> DROPPED_PLAYER = new Ephemeral2<>();
    @Unique
    private static final Ephemeral2<Boolean> NUDGE_UP_COUNT = new Ephemeral2<>();

    @WrapOperation(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> checkIfPlayerDrop(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, Operation<List<ItemStack>> original) {
        List<ItemStack> results = original.call(state, level, pos, blockEntity, entity, tool);
        if (!results.isEmpty() && entity instanceof ServerPlayer serverPlayer) {
            DROPPED_PLAYER.push(serverPlayer, results.size());
            NUDGE_UP_COUNT.push(true, results.size());
        }
        return results;
    }

    @WrapOperation(method = "method_36990", at = @At(value = "NEW", target = "Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static ItemEntity nudgeTowardsPlayer(Level level, double x, double y, double z, ItemStack stack, Operation<ItemEntity> original) {
        ItemEntity entity = original.call(level, x, y, z, stack);

        if (DROPPED_PLAYER.hasValue()) {
            ServerPlayer source = DROPPED_PLAYER.pop();
            if (JSSTConfig.INSTANCE.instance().itemNudging.shiftTowardsPlayer) {
                Vec3 offset = source.position().subtract(entity.position()).normalize().scale(0.15f);

                entity.setDeltaMovement(offset.x, entity.getDeltaMovement().y, offset.z);
            }
        }

        return entity;
    }

    @WrapOperation(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;nextDouble(Lnet/minecraft/util/RandomSource;DD)D", ordinal = 1))
    private static double changeMinimumHeight(RandomSource random, double minimum, double maximum, Operation<Double> original) {
        if (NUDGE_UP_COUNT.hasValue() && NUDGE_UP_COUNT.pop() && JSSTConfig.INSTANCE.instance().itemNudging.shiftUp) {
            minimum = 0.125;
        }
        return original.call(random, minimum, maximum);
    }
}
