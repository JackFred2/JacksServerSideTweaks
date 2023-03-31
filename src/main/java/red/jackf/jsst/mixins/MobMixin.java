package red.jackf.jsst.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.displayitems.DisplayItems;

/**
 * Used by:
 * DisplayItems - Prevent mobs picking up display items
 */
@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "wantsToPickUp(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void jsst_dontPickupDisplayItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var shouldCheck = JSST.CONFIG.get().displayItems.enabled && JSST.CONFIG.get().displayItems.ownerPickupOnly;
        if (shouldCheck && DisplayItems.isDisplayItem(stack)) cir.setReturnValue(false);
    }
}
