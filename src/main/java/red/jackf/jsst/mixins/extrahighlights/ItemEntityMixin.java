package red.jackf.jsst.mixins.extrahighlights;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.impl.feature.extrahighlights.SugarcaneHighlights;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void highlightIfSugarcane(CallbackInfo ci) {
        if (!this.isRemoved() && this.getItem().is(Items.SUGAR_CANE) && this.level() instanceof ServerLevel serverLevel) {
            SugarcaneHighlights.onSugarcaneItemTick(serverLevel, (ItemEntity) (Object) this);
        }
    }
}
