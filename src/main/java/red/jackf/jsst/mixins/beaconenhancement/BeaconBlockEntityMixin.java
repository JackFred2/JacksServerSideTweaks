package red.jackf.jsst.mixins.beaconenhancement;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
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
    private static void stopFilteringEffects(@Nullable Holder<MobEffect> effect, CallbackInfoReturnable<Holder<MobEffect>> cir) {
        if (JSSTConfig.INSTANCE.instance().beaconEnhancement.enabled) cir.setReturnValue(effect);
    }

    @ModifyExpressionValue(method = "updateBase", at = @At(value = "CONSTANT", args = "intValue=4"))
    private static int allowLargerBaseSize(int original) {
        var config = JSSTConfig.INSTANCE.instance().beaconEnhancement;
        return config.enabled ? config.maxLevel : original;
    }

    @ModifyExpressionValue(method = "applyEffects", at = @At(value = "CONSTANT", args = "intValue=4"), require = 2)
    private static int applySecondaryAtChangedSizes(int original) {
        var config = JSSTConfig.INSTANCE.instance().beaconEnhancement;
        return config.enabled ? config.secondPowerMinLevel : original;
    }

    @Definition(id = "d", local = @Local(type = double.class))
    @Definition(id = "inflate", method = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;")
    @Expression("?.inflate(@(d))")
    @ModifyExpressionValue(method = "applyEffects", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static double increaseBeaconRange(double original) {
        var config = JSSTConfig.INSTANCE.instance().miscellaneous.beaconRangeModifier;
        return config * original;
    }
}
