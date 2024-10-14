package red.jackf.jsst.impl.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.*;

public interface Banners {
    interface ByColour {
        Map<DyeColor, BannerBlock> FLOOR = new HashMap<>();
        Map<DyeColor, WallBannerBlock> WALL = new HashMap<>();
        Map<DyeColor, BannerItem> ITEM = new HashMap<>();
    }

    interface PMC {
        private static BiMap<Character, DyeColor> makeColours() {
            BiMap<Character, DyeColor> map = HashBiMap.create();
            map.put('1', DyeColor.BLACK);
            map.put('2', DyeColor.RED);
            map.put('3', DyeColor.GREEN);
            map.put('4', DyeColor.BROWN);
            map.put('5', DyeColor.BLUE);
            map.put('6', DyeColor.PURPLE);
            map.put('7', DyeColor.CYAN);
            map.put('8', DyeColor.LIGHT_GRAY);
            map.put('9', DyeColor.GRAY);
            map.put('a', DyeColor.PINK);
            map.put('b', DyeColor.LIME);
            map.put('c', DyeColor.YELLOW);
            map.put('d', DyeColor.LIGHT_BLUE);
            map.put('e', DyeColor.MAGENTA);
            map.put('f', DyeColor.ORANGE);
            map.put('g', DyeColor.WHITE);
            return map;
        }

        BiMap<Character, DyeColor> COLOURS = makeColours();

        private static BiMap<Character, ResourceKey<BannerPattern>> makePatterns() {
            BiMap<Character, ResourceKey<BannerPattern>> map = HashBiMap.create();
            map.put('o', BannerPatterns.STRIPE_BOTTOM);
            map.put('v', BannerPatterns.STRIPE_TOP);
            map.put('s', BannerPatterns.STRIPE_LEFT);
            map.put('u', BannerPatterns.STRIPE_RIGHT);
            map.put('p', BannerPatterns.STRIPE_CENTER);
            map.put('t', BannerPatterns.STRIPE_MIDDLE);
            map.put('r', BannerPatterns.STRIPE_DOWNRIGHT);
            map.put('q', BannerPatterns.STRIPE_DOWNLEFT);
            map.put('i', BannerPatterns.STRIPE_SMALL);
            map.put('n', BannerPatterns.STRAIGHT_CROSS);
            map.put('7', BannerPatterns.CROSS);
            map.put('a', BannerPatterns.DIAGONAL_RIGHT_MIRROR);
            map.put('9', BannerPatterns.DIAGONAL_LEFT);
            map.put('A', BannerPatterns.DIAGONAL_LEFT_MIRROR);
            map.put('B', BannerPatterns.DIAGONAL_RIGHT);
            map.put('e', BannerPatterns.HALF_VERTICAL);
            map.put('E', BannerPatterns.HALF_VERTICAL_MIRROR);
            map.put('d', BannerPatterns.HALF_HORIZONTAL);
            map.put('D', BannerPatterns.HALF_HORIZONTAL_MIRROR);
            map.put('j', BannerPatterns.SQUARE_BOTTOM_LEFT);
            map.put('k', BannerPatterns.SQUARE_BOTTOM_RIGHT);
            map.put('l', BannerPatterns.SQUARE_TOP_LEFT);
            map.put('m', BannerPatterns.SQUARE_TOP_RIGHT);
            map.put('y', BannerPatterns.TRIANGLE_BOTTOM);
            map.put('z', BannerPatterns.TRIANGLE_TOP);
            map.put('w', BannerPatterns.TRIANGLES_BOTTOM);
            map.put('x', BannerPatterns.TRIANGLES_TOP);
            map.put('5', BannerPatterns.CIRCLE_MIDDLE);
            map.put('g', BannerPatterns.RHOMBUS_MIDDLE);
            map.put('3', BannerPatterns.BORDER);
            map.put('8', BannerPatterns.CURLY_BORDER);
            map.put('4', BannerPatterns.BRICKS);
            map.put('6', BannerPatterns.CREEPER);
            map.put('h', BannerPatterns.SKULL);
            map.put('b', BannerPatterns.FLOWER);
            map.put('f', BannerPatterns.MOJANG);
            map.put('F', BannerPatterns.GLOBE);
            map.put('G', BannerPatterns.PIGLIN);
            map.put('c', BannerPatterns.GRADIENT);
            map.put('C', BannerPatterns.GRADIENT_UP);
            return map;
        }

        BiMap<Character, ResourceKey<BannerPattern>> PATTERNS = makePatterns();

        static DataResult<Pair<DyeColor, List<Pair<Holder<BannerPattern>, DyeColor>>>> parsePMCCode(RegistryAccess.Frozen registries, String code) {
            if (code.length() % 2 == 0) return DataResult.error(() -> "Code incorrect length");

            char backgroundChar = code.charAt(0);
            if (!COLOURS.containsKey(backgroundChar)) return DataResult.error(() -> "Invalid colour '%s'".formatted(backgroundChar));
            DyeColor background = COLOURS.get(backgroundChar);

            Registry<BannerPattern> registry = registries.registryOrThrow(Registries.BANNER_PATTERN);
            List<Pair<Holder<BannerPattern>, DyeColor>> layers = new ArrayList<>();

            for (int i = 1; i < code.length(); i += 2) {
                char layerColourChar = code.charAt(i);
                if (!COLOURS.containsKey(layerColourChar)) return DataResult.error(() -> "Invalid colour '%s'".formatted(layerColourChar));
                DyeColor layerColour = COLOURS.get(layerColourChar);

                char layerPatternChar = code.charAt(i + 1);
                if (!PATTERNS.containsKey(layerPatternChar)) return DataResult.error(() -> "Invalid pattern '%s'".formatted(layerPatternChar));
                ResourceKey<BannerPattern> layerPatternKey = PATTERNS.get(layerPatternChar);
                Optional<Holder.Reference<BannerPattern>> layerPattern = registry.getHolder(layerPatternKey);
                if (layerPattern.isEmpty()) return DataResult.error(() -> "Invalid pattern key '%s'".formatted(layerPatternKey.location()));
                layers.add(Pair.of(layerPattern.get(), layerColour));
            }

            return DataResult.success(Pair.of(background, layers));
        }
    }
}
