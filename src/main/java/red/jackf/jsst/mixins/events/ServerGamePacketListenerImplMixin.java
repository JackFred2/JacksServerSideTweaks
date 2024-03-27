package red.jackf.jsst.mixins.events;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.events.ChangeCarriedItem;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
    private void jsst$saveOldSlot(ServerboundSetCarriedItemPacket ignored, CallbackInfo ci, @Share("oldSlot") LocalIntRef oldSlot) {
        oldSlot.set(getPlayer().getInventory().selected);
    }

    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.AFTER))
    private void jsst$hookSetCarriedEvent(ServerboundSetCarriedItemPacket packet, CallbackInfo ci, @Share("oldSlot") LocalIntRef oldSlot) {
        ChangeCarriedItem.EVENT.invoker().changeCarriedItem(this.getPlayer(), oldSlot.get(), packet.getSlot());
    }
}
