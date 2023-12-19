package red.jackf.jsst.mixins.beaconrange;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import red.jackf.jsst.feature.beaconrange.ExtendedBeaconRange;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

    @ModifyArg(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private static double jsst$modifyBeaconRange(double range) {
        return range * ExtendedBeaconRange.INSTANCE.getMultiplier();
    }
}
