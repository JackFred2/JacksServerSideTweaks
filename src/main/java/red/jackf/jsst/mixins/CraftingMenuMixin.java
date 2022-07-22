package red.jackf.jsst.mixins;

import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.features.portablecraftingtable.JSSTAlwaysValidable;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin implements JSSTAlwaysValidable {
    @Unique
    private boolean jsst_alwaysValid = false;

    public void jsst_setAlwaysValid() {
        this.jsst_alwaysValid = true;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void jsst_stillValid(CallbackInfoReturnable<Boolean> cir) {
        if (this.jsst_alwaysValid) cir.setReturnValue(Boolean.TRUE);
    }
}
