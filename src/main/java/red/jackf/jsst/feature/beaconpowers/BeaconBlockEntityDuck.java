package red.jackf.jsst.feature.beaconpowers;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface BeaconBlockEntityDuck {
    static int getPowerLevel(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$getPowerLevel();
    }

    static @Nullable MobEffect getPrimaryPower(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$getPrimaryPower();
    }

    static @Nullable MobEffect getSecondaryPower(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$getSecondaryPower();
    }

    int jsst$getPowerLevel();

    @Nullable MobEffect jsst$getPrimaryPower();

    @Nullable MobEffect jsst$getSecondaryPower();
}
