package red.jackf.jsst.mixins.beaconpowers;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import red.jackf.jsst.feature.beaconpowers.BeaconBlockEntityDuck;

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
}
