package red.jackf.jsst.mixins.anvilenhancement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.feature.anvilenhancement.AnvilEnhancement;
import red.jackf.jsst.util.sgui.Util;

@Debug(export = true)
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;

    public AnvilMenuMixin(
            @Nullable MenuType<?> type,
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
        throw new AssertionError("mixin failed");
    }

    @Inject(method = "createResult",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/inventory/DataSlot;get()I", ordinal = 1))
    private void jsst$anvilenhancement$adjustPriceIfRenameOnly(CallbackInfo ci,
                                                               @Local(ordinal = 0) int workCost, // i
                                                               @Local(ordinal = 1) int baseCost, // j
                                                               @Local(ordinal = 2) int renameCost) { // k
        AnvilEnhancement.RenameCost cost = AnvilEnhancement.INSTANCE.config().renameCost;
        if (cost == AnvilEnhancement.RenameCost.vanilla) return;

        boolean isRenameOnly = renameCost > 0 && workCost == renameCost;

        if (isRenameOnly) this.cost.set(cost == AnvilEnhancement.RenameCost.one_level ? 1 : 0);
    }

    // bit hacky but not sure how to modify cost.get() > 0
    @ModifyExpressionValue(method = "mayPickup",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I", ordinal = 1))
    private int jsst$anvilenhancement$allowPickupIfFreeByForcingGT0True(int actualCost) {
        if (AnvilEnhancement.INSTANCE.config().renameCost == AnvilEnhancement.RenameCost.free) return 1;
        return actualCost;
    }

    // in vanilla anvil costs aren't synced but are calculated on each side
    @Inject(method = "createResult", at = @At("TAIL"))
    private void jsst$anvilenhancement$sendCorrectCost(CallbackInfo ci) {
        if (AnvilEnhancement.INSTANCE.config().renameCost != AnvilEnhancement.RenameCost.vanilla && this.player instanceof ServerPlayer serverPlayer)
            Util.sendAnvilCost(serverPlayer, this.containerId, this.cost.get());
    }
}
