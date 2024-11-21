package red.jackf.jsst.mixins.beaconenhancement;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.jsst.impl.config.JSSTConfig;

@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin {
    @Definition(id = "inflate", method = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;")
    @Expression("?.inflate(@((double) ?))")
    @ModifyExpressionValue(method = "applyEffects", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static double increaseBeaconRangeWhenGrabbing(double original) {
        var config = JSSTConfig.INSTANCE.instance().beaconEnhancement;
        return config.enabled ? config.conduitRangeModifier * original : original;
    }

    @Definition(id = "closerThan", method = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z")
    @Expression("?.closerThan(?, @((double) ?))")
    @ModifyExpressionValue(method = "applyEffects", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static double increaseBeaconRangeWhenChecking(double original) {
        var config = JSSTConfig.INSTANCE.instance().beaconEnhancement;
        return config.enabled ? config.conduitRangeModifier * original : original;
    }
}
