package red.jackf.jsst.mixins.anvilenhancement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.feature.anvilenhancement.AnvilEnhancement;
import red.jackf.jsst.util.sgui.Util;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Unique private boolean isRenameOnly;
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
    private void jsst$anvilenhancement$adjustPrice(CallbackInfo ci,
                                                               @Local(ordinal = 0) int workCost, // i
                                                               // @Local(ordinal = 1) int baseCost, // j
                                                               @Local(ordinal = 2) int renameCost) { // k
        AnvilEnhancement.RenameCost cost = AnvilEnhancement.INSTANCE.config().renameCost;
        if (cost == AnvilEnhancement.RenameCost.vanilla) return;

        this.isRenameOnly = renameCost > 0 && workCost == renameCost;

        if (this.isRenameOnly) {
            this.cost.set(cost == AnvilEnhancement.RenameCost.one_level ? 1 : 0);
        } else if (renameCost == 1 && cost == AnvilEnhancement.RenameCost.free) {
            this.cost.set(this.cost.get() - 1);
        }
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

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    private void jsst$anvilenhancement$preventBreak(
            Player player,
            ItemStack stack,
            CallbackInfo ci) {
        if (AnvilEnhancement.INSTANCE.config().renameDoesNotDamageAnvil && this.isRenameOnly) {
            ci.cancel();
            this.isRenameOnly = false;
            this.access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1.4f));
        }
    }
}
