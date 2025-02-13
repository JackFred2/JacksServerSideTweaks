package red.jackf.jsst.mixins.saplingreplant;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.saplingreplant.SaplingReplant;
import red.jackf.jsst.impl.utils.RegistryUtils;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
        throw new AssertionError("Mixin constructor called!");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
    private void onTickInject(CallbackInfo ci) {
        var config = JSSTConfig.INSTANCE.instance().saplingReplant;
        if (config.enabled && RegistryUtils.parseTag(Registries.ITEM, config.saplingTag).map(this.getItem()::is).orElse(false)) {
            SaplingReplant.possiblyReplant((ItemEntity) (Object) this);
        }
    }
}
