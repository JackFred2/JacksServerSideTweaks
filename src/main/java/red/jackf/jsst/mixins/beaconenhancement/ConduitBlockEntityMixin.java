package red.jackf.jsst.mixins.beaconenhancement;

import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import red.jackf.jsst.feature.beaconenhancement.BeaconEnhancement;

@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin {
    @ModifyArg(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private static double jsst$beaconEnhancement$modifyConduitAABBInflation(double range) {
        if (BeaconEnhancement.INSTANCE.config().enabled) {
            return range * BeaconEnhancement.INSTANCE.config().conduitRangeMultiplier;
        } else {
            return range;
        }
    }

    @ModifyArg(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"))
    private static double jsst$beaconEnhancement$modifyConduitPlayerCheckRadius(double radius) {
        if (BeaconEnhancement.INSTANCE.config().enabled) {
            return radius * BeaconEnhancement.INSTANCE.config().conduitRangeMultiplier;
        } else {
            return radius;
        }
    }

}
