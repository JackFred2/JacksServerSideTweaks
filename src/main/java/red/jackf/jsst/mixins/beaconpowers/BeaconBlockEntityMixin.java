package red.jackf.jsst.mixins.beaconpowers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.feature.beaconpowers.BeaconBlockEntityDuck;
import red.jackf.jsst.feature.beaconpowers.MoreBeaconPowers;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin implements BeaconBlockEntityDuck {
    @Shadow
    int levels;

    @Shadow
    @Nullable MobEffect primaryPower;

    @Shadow
    @Nullable MobEffect secondaryPower;

    @Override
    public int jsst$getPowerLevel() {
        return this.levels;
    }

    @Override
    public @Nullable MobEffect jsst$getPrimaryPower() {
        return this.primaryPower;
    }

    @Override
    public @Nullable MobEffect jsst$getSecondaryPower() {
        return this.secondaryPower;
    }

    @Override
    public void jsst$setPrimaryPower(MobEffect effect) {
        this.primaryPower = effect;
    }

    @Override
    public void jsst$setSecondaryPower(MobEffect effect) {
        this.secondaryPower = effect;
    }

    @Inject(method = "filterEffect", at = @At("HEAD"), cancellable = true)
    private static void jsst$beaconpowers$changefilter(MobEffect effect, CallbackInfoReturnable<@Nullable MobEffect> cir) {
        if (MoreBeaconPowers.INSTANCE.config().enabled) {
            cir.setReturnValue(MoreBeaconPowers.INSTANCE.config().powers.isValid(effect) ? effect : null);
        }
    }

    @ModifyExpressionValue(method = "updateBase", at = @At(value = "CONSTANT", args = "intValue=4"))
    private static int jsst$beaconpowers$changeMaxLevel(int maxLevel) {
        if (MoreBeaconPowers.INSTANCE.config().enabled) {
            return MoreBeaconPowers.INSTANCE.config().maxBeaconLevel;
        } else {
            return maxLevel;
        }
    }
}
