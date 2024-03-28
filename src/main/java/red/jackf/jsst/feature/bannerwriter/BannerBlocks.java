package red.jackf.jsst.feature.bannerwriter;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;

import java.util.HashMap;
import java.util.Map;

public interface BannerBlocks {
    Map<DyeColor, BannerBlock> FLOOR = new HashMap<>();
    Map<DyeColor, WallBannerBlock> WALL = new HashMap<>();
}
