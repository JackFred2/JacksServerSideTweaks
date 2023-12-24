package red.jackf.jsst.feature.beaconenhancement;

import net.minecraft.world.LockCode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface BeaconBlockEntityDuck {
    static int getPowerLevel(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$getPowerLevel();
    }

    static @Nullable MobEffect getPrimaryPower(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$getPrimaryPower();
    }

    static @Nullable MobEffect getSecondaryPower(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$getSecondaryPower();
    }

    static void setPrimaryPower(BeaconBlockEntity beaconBlockEntity, MobEffect effect) {
        ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$setPrimaryPower(effect);
    }

    static void setSecondaryPower(BeaconBlockEntity beaconBlockEntity, MobEffect effect) {
        ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$setSecondaryPower(effect);
    }

    static LockCode getLock(BeaconBlockEntity beaconBlockEntity) {
        return ((BeaconBlockEntityDuck) beaconBlockEntity).jsst$beaconEnhancement$getLock();
    }

    int jsst$beaconEnhancement$getPowerLevel();

    @Nullable MobEffect jsst$beaconEnhancement$getPrimaryPower();

    @Nullable MobEffect jsst$beaconEnhancement$getSecondaryPower();

    void jsst$beaconEnhancement$setPrimaryPower(MobEffect effect);

    void jsst$beaconEnhancement$setSecondaryPower(MobEffect effect);

    LockCode jsst$beaconEnhancement$getLock();
}
