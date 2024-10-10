package red.jackf.jsst.mixins.portablecrafting;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.impl.feature.portablecrafting.PortableCrafting;
import red.jackf.jsst.impl.mixinutils.JSSTItemValidation;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin implements JSSTItemValidation {
    @Unique
    @Nullable
    private InteractionHand handToCheck = null;

    @Override
    public void jsst$markAsItemValidation(InteractionHand hand) {
        this.handToCheck = hand;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void checkIfHandItemIsUsed(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (handToCheck != null && PortableCrafting.isValidCraftingTable(player.registryAccess(), player.getItemInHand(handToCheck))) {
            cir.setReturnValue(true);
        }
    }
}
