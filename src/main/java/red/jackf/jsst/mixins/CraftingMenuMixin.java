package red.jackf.jsst.mixins;

import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.portablecraftingtable.JSSTAlwaysValidable;

/**
 * Adds a settable flag to CraftingMenu that prevents it needing a physical block in-world to stay open.
 */
@Mixin(CraftingMenu.class)
public class CraftingMenuMixin implements JSSTAlwaysValidable {
    @Unique
    private boolean jsst_alwaysValid = false;

    @Unique
    private InteractionHand jsst_lastUsedHand = InteractionHand.MAIN_HAND;

    public void jsst_setAlwaysValid(InteractionHand usedHand) {
        this.jsst_alwaysValid = true;
        this.jsst_lastUsedHand = usedHand;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void jsst_stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.jsst_alwaysValid && JSST.CONFIG_HANDLER.get().portableCrafting.items.contains(Registry.ITEM.getKey(player.getItemInHand(this.jsst_lastUsedHand).getItem())))
            cir.setReturnValue(Boolean.TRUE);
    }
}
