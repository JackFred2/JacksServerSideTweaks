package red.jackf.jsst.mixins;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import red.jackf.jsst.JSST;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @ModifyVariable(method = "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/effect/MobEffect;Lnet/minecraft/world/effect/MobEffect;)V",
            at = @At(value = "CONSTANT", args = "intValue=0", shift = At.Shift.AFTER))
    private static double jsst_modifyBeaconRange(double in, Level level) {
        var config = JSST.CONFIG.get().beaconRangeModifier;
        if (!config.enabled) return in;
        return in * config.rangeMultiplier;
    }
}
