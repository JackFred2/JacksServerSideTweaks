package red.jackf.jsst.mixins.bannerwriter;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.feature.bannerwriter.BannerBlocks;

@Mixin(AbstractBannerBlock.class)
public class AbstractBannerBlockMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void jsst$bannerwriter$saveBannerColours(DyeColor color, BlockBehaviour.Properties properties, CallbackInfo ci) {
        Object downcast = this;
        if (downcast instanceof BannerBlock floorBlock) {
            BannerBlocks.FLOOR.put(color, floorBlock);
        } else if (downcast instanceof WallBannerBlock wallBlock) {
            BannerBlocks.WALL.put(color, wallBlock);
        }
    }
}
