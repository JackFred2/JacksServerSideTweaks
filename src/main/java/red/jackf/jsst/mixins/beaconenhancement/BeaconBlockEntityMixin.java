package red.jackf.jsst.mixins.beaconenhancement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.feature.beaconenhancement.BeaconBlockEntityDuck;
import red.jackf.jsst.feature.beaconenhancement.BeaconEnhancement;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin implements BeaconBlockEntityDuck {
    @Shadow
    int levels;

    @Shadow
    @Nullable MobEffect primaryPower;

    @Shadow
    @Nullable MobEffect secondaryPower;

    @Override
    public int jsst$beaconEnhancement$getPowerLevel() {
        return this.levels;
    }

    @Override
    public @Nullable MobEffect jsst$beaconEnhancement$getPrimaryPower() {
        return this.primaryPower;
    }

    @Override
    public @Nullable MobEffect jsst$beaconEnhancement$getSecondaryPower() {
        return this.secondaryPower;
    }

    @Override
    public void jsst$beaconEnhancement$setPrimaryPower(MobEffect effect) {
        this.primaryPower = effect;
    }

    @Override
    public void jsst$beaconEnhancement$setSecondaryPower(MobEffect effect) {
        this.secondaryPower = effect;
    }

    @Inject(method = "filterEffect", at = @At("HEAD"), cancellable = true)
    private static void jsst$beaconEnhancement$changeFilter(MobEffect effect, CallbackInfoReturnable<@Nullable MobEffect> cir) {
        if (BeaconEnhancement.INSTANCE.config().enabled) {
            cir.setReturnValue(BeaconEnhancement.INSTANCE.config().powers.isValid(effect) ? effect : null);
        }
    }

    @ModifyExpressionValue(method = "updateBase", at = @At(value = "CONSTANT", args = "intValue=4"))
    private static int jsst$beaconEnhancement$changeMaxLevel(int maxLevel) {
        if (BeaconEnhancement.INSTANCE.config().enabled) {
            return BeaconEnhancement.INSTANCE.config().maxBeaconLevel;
        } else {
            return maxLevel;
        }
    }

    @ModifyArg(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private static double jsst$beaconEnhancement$modifyBeaconRange(double range) {
        if (BeaconEnhancement.INSTANCE.config().enabled) {
            return range * BeaconEnhancement.INSTANCE.config().rangeMultiplier;
        } else {
            return range;
        }
    }
}
