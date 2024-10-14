package red.jackf.jsst.mixins.bannerwriter;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.impl.feature.bannerwriter.BannerWriter;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place",
    at = @At(value = "INVOKE",
            //? if <=1.20.1 {
            /*target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"))
            *///?} else
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"))
private void updateBannerIfNeeded(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer && context.getLevel() instanceof ServerLevel serverLevel) {
            BannerWriter.onPlayerPlace(serverPlayer, serverLevel, context.getClickedPos(), context.getItemInHand());
        }
    }
}
