package red.jackf.jsst.mixins.beaconenhancement;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.impl.config.JSSTConfig;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

    @Inject(method = "filterEffect", at = @At("HEAD"), cancellable = true)
    private static void allowAllEffects(@Nullable Holder<MobEffect> effect, CallbackInfoReturnable<Holder<MobEffect>> cir) {
        if (JSSTConfig.INSTANCE.instance().beaconEnhancement.enabled) cir.setReturnValue(effect);
    }
}
