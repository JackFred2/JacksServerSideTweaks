package red.jackf.jsst.mixins;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.features.portablecrafting.JSSTInventoryItemValidable;
import red.jackf.jsst.features.portablecrafting.PortableCrafting;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin implements JSSTInventoryItemValidable {

    @Unique
    private boolean itemValidation = false;
    @Unique
    private InteractionHand handToCheck = InteractionHand.MAIN_HAND;

    @Unique
    public void setItemValidation(InteractionHand handToCheck) {
        this.itemValidation = true;
        this.handToCheck = handToCheck;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void jsst_checkIfItemValidation(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (itemValidation && player.getItemInHand(handToCheck).is(PortableCrafting.CRAFTING_TABLES)) cir.setReturnValue(true);
    }
}
