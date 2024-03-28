package red.jackf.jsst.mixins.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.events.AfterPlacePlaceBlock;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V",
                    shift = At.Shift.AFTER))
    private void jsst$afterPlayerPlaceBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> ci) {
        if (context.getLevel() instanceof ServerLevel serverLevel && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            AfterPlacePlaceBlock.EVENT.invoker().afterPlayerPlaceBlock(serverPlayer,
                    serverLevel,
                    context.getClickedPos(),
                    serverLevel.getBlockState(context.getClickedPos()),
                    context.getItemInHand());
        }
    }
}
