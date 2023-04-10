package red.jackf.jsst.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.features.itemeditor.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.JSSTSealableMenuWithButtons;

import java.util.Map;

/**
 * Used by:
 * ItemEditor - Add hooks to slot clicks and prevent lifting items from the "menu".
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements JSSTSealableMenuWithButtons {
    @Nullable
    @Unique
    private Map<Integer, ItemGuiElement> buttons = null;

    @Inject(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
    private void jsst_checkForSlotHooks(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (buttons != null) {
            if (buttons.containsKey(slotId) && button == 0 && clickType == ClickType.PICKUP) {
                var runnable = buttons.get(slotId).onClick();
                if (runnable != null) runnable.run();
            }
            ci.cancel();
        }
    }

    @Override
    public void jsst_sealWithButtons(Map<Integer, ItemGuiElement> buttons) {
        this.buttons = buttons;
    }
}
