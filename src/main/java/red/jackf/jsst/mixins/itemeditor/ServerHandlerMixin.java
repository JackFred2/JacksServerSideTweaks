package red.jackf.jsst.mixins.itemeditor;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.util.sgui.menus.StringInputMenu;

// reimplement SGUI's mixin but slightly differently for more control
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerHandlerMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleRenameItem", at = @At("TAIL"))
    private void jsst$itemeditor$listenForRenameItem(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler vsh) {
            try {
                if (vsh.getGui() instanceof StringInputMenu menu) {
                    menu.onTextUpdate(packet.getName());
                }
            } catch (Throwable throwable) {
                vsh.getGui().handleException(throwable);
            }
        }
    }
}
