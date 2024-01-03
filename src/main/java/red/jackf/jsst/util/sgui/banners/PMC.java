package red.jackf.jsst.util.sgui.banners;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

class PMC {
    protected static final BiMap<Character, DyeColor> COLOURS = HashBiMap.create(16);
    static {
        COLOURS.put('1', DyeColor.BLACK);
        COLOURS.put('2', DyeColor.RED);
        COLOURS.put('3', DyeColor.GREEN);
        COLOURS.put('4', DyeColor.BROWN);
        COLOURS.put('5', DyeColor.BLUE);
        COLOURS.put('6', DyeColor.PURPLE);
        COLOURS.put('7', DyeColor.CYAN);
        COLOURS.put('8', DyeColor.LIGHT_GRAY);
        COLOURS.put('9', DyeColor.GRAY);
        COLOURS.put('a', DyeColor.PINK);
        COLOURS.put('b', DyeColor.LIME);
        COLOURS.put('c', DyeColor.YELLOW);
        COLOURS.put('d', DyeColor.LIGHT_BLUE);
        COLOURS.put('e', DyeColor.MAGENTA);
        COLOURS.put('f', DyeColor.ORANGE);
        COLOURS.put('g', DyeColor.WHITE);
    }
    protected static final BiMap<Character, ResourceKey<BannerPattern>> PATTERNS = HashBiMap.create(40);
    static {
        PATTERNS.put('o', BannerPatterns.STRIPE_BOTTOM);
        PATTERNS.put('v', BannerPatterns.STRIPE_TOP);
        PATTERNS.put('s', BannerPatterns.STRIPE_LEFT);
        PATTERNS.put('u', BannerPatterns.STRIPE_RIGHT);
        PATTERNS.put('p', BannerPatterns.STRIPE_CENTER);
        PATTERNS.put('t', BannerPatterns.STRIPE_MIDDLE);
        PATTERNS.put('r', BannerPatterns.STRIPE_DOWNRIGHT);
        PATTERNS.put('q', BannerPatterns.STRIPE_DOWNLEFT);
        PATTERNS.put('i', BannerPatterns.STRIPE_SMALL);
        PATTERNS.put('n', BannerPatterns.STRAIGHT_CROSS);
        PATTERNS.put('7', BannerPatterns.CROSS);
        PATTERNS.put('a', BannerPatterns.DIAGONAL_RIGHT_MIRROR);
        PATTERNS.put('9', BannerPatterns.DIAGONAL_LEFT);
        PATTERNS.put('A', BannerPatterns.DIAGONAL_LEFT_MIRROR);
        PATTERNS.put('B', BannerPatterns.DIAGONAL_RIGHT);
        PATTERNS.put('e', BannerPatterns.HALF_VERTICAL);
        PATTERNS.put('E', BannerPatterns.HALF_VERTICAL_MIRROR);
        PATTERNS.put('d', BannerPatterns.HALF_HORIZONTAL);
        PATTERNS.put('D', BannerPatterns.HALF_HORIZONTAL_MIRROR);
        PATTERNS.put('j', BannerPatterns.SQUARE_BOTTOM_LEFT);
        PATTERNS.put('k', BannerPatterns.SQUARE_BOTTOM_RIGHT);
        PATTERNS.put('l', BannerPatterns.SQUARE_TOP_LEFT);
        PATTERNS.put('m', BannerPatterns.SQUARE_TOP_RIGHT);
        PATTERNS.put('y', BannerPatterns.TRIANGLE_BOTTOM);
        PATTERNS.put('z', BannerPatterns.TRIANGLE_TOP);
        PATTERNS.put('w', BannerPatterns.TRIANGLES_BOTTOM);
        PATTERNS.put('x', BannerPatterns.TRIANGLES_TOP);
        PATTERNS.put('5', BannerPatterns.CIRCLE_MIDDLE);
        PATTERNS.put('g', BannerPatterns.RHOMBUS_MIDDLE);
        PATTERNS.put('3', BannerPatterns.BORDER);
        PATTERNS.put('8', BannerPatterns.CURLY_BORDER);
        PATTERNS.put('4', BannerPatterns.BRICKS);
        PATTERNS.put('6', BannerPatterns.CREEPER);
        PATTERNS.put('h', BannerPatterns.SKULL);
        PATTERNS.put('b', BannerPatterns.FLOWER);
        PATTERNS.put('f', BannerPatterns.MOJANG);
        PATTERNS.put('F', BannerPatterns.GLOBE);
        PATTERNS.put('G', BannerPatterns.PIGLIN);
        PATTERNS.put('c', BannerPatterns.GRADIENT);
        PATTERNS.put('C', BannerPatterns.GRADIENT_UP);
    }

    protected static final String DEFAULT = "g";
}
