package red.jackf.jsst.mixins.bannerwriter;

import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.impl.utils.Banners;

@Mixin(BannerItem.class)
public abstract class BannerItemMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void saveBannerByColour(Block block, Block wallBlock, Item.Properties properties, CallbackInfo ci) {
        DyeColor color = ((BannerBlock) block).getColor();

        Banners.ByColour.ITEM.put(color, (BannerItem) (Object) this);
        Banners.ByColour.FLOOR.put(color, (BannerBlock) block);
        Banners.ByColour.WALL.put(color, (WallBannerBlock) wallBlock);
    }
}
