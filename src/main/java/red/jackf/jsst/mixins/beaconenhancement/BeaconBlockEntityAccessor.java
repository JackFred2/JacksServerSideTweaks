package red.jackf.jsst.mixins.beaconenhancement;

import net.minecraft.core.Holder;
import net.minecraft.world.LockCode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
    @Accessor
    LockCode getLockKey();

    @Accessor
    int getLevels();

    @Accessor
    ContainerData getDataAccess();

    @Accessor
    @Nullable Holder<MobEffect> getPrimaryPower();

    @Accessor
    @Nullable Holder<MobEffect> getSecondaryPower();
}
