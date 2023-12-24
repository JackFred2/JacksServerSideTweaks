package red.jackf.jsst.mixins.itemmovement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import red.jackf.jsst.feature.qualityoflife.QualityOfLife;

@Mixin(Block.class)
public class BlockMixin {

    @ModifyExpressionValue(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;nextDouble(Lnet/minecraft/util/RandomSource;DD)D", ordinal = 1))
    private static double jsst$itemMovement$moveUp(double in) {
        if (QualityOfLife.INSTANCE.config().minedItemsShiftUp)
            return Math.max(0.125, in);
        else
            return in;
    }
}
