package red.jackf.jsst.mixins.itemeditor.tempsguifix;

import eu.pb4.sgui.api.GuiHelpers;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiHelpers.class)
public class GuiHelperMixin {
    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private static void jsst$fixsmithingheight(MenuType<?> type, CallbackInfoReturnable<Integer> cir) {
        if (MenuType.SMITHING.equals(type)) cir.setReturnValue(1);
    }

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private static void jsst$fixsmithingwidth(MenuType<?> type, CallbackInfoReturnable<Integer> cir) {
        if (MenuType.SMITHING.equals(type)) cir.setReturnValue(4);
    }
}
