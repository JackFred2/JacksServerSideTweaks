package red.jackf.jsst.mixins.qualityoflife;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.jsst.feature.qualityoflife.QualityOfLife;
import red.jackf.jsst.util.Ephemeral2;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Block.class)
public class BlockMixin {
    @Unique
    private static final Ephemeral2<ServerPlayer> SOURCE_PLAYER = new Ephemeral2<>();


    @ModifyExpressionValue(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;nextDouble(Lnet/minecraft/util/RandomSource;DD)D", ordinal = 1))
    private static double jsst$qol$doMinedItemsShiftUp(double in) {
        if (QualityOfLife.INSTANCE.config().doMinedItemsShiftUp)
            return Math.max(0.125, in);
        else
            return in;
    }

    @WrapOperation(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private static void jsst$qol$doMinedItemsShiftTowardsPlayer$markPlayer(
            List<ItemStack> items,
            Consumer<ItemStack> forEach,
            Operation<Void> original,
            BlockState state, Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity) {
        if (QualityOfLife.INSTANCE.config().doMinedItemsShiftTowardsPlayer && entity instanceof ServerPlayer serverPlayer) {
            SOURCE_PLAYER.push(serverPlayer, items.size());
        }
        original.call(items, forEach);
    }

    @ModifyReturnValue(method = "method_36990", at = @At("RETURN"))
    private static ItemEntity jsst$qol$doMinedItemsShiftTowardsPlayer$applyPlayer(ItemEntity in) {
        if (QualityOfLife.INSTANCE.config().doMinedItemsShiftTowardsPlayer && SOURCE_PLAYER.hasValue()) {
            Vec3 vector = SOURCE_PLAYER.pop().position().subtract(in.position()).normalize();
            in.setDeltaMovement(vector.x * 0.1, 0.2, vector.z * 0.1);
        }

        return in;
    }
}
