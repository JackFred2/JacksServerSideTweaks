package red.jackf.jsst.features.itemeditor.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.itemeditor.menus.ColourMenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BannerUtils {
    private static final BiMap<Character, DyeColor> PMC_COLOURS = HashBiMap.create(16);
    static {
        PMC_COLOURS.put('1', DyeColor.BLACK);
        PMC_COLOURS.put('2', DyeColor.RED);
        PMC_COLOURS.put('3', DyeColor.GREEN);
        PMC_COLOURS.put('4', DyeColor.BROWN);
        PMC_COLOURS.put('5', DyeColor.BLUE);
        PMC_COLOURS.put('6', DyeColor.PURPLE);
        PMC_COLOURS.put('7', DyeColor.CYAN);
        PMC_COLOURS.put('8', DyeColor.LIGHT_GRAY);
        PMC_COLOURS.put('9', DyeColor.GRAY);
        PMC_COLOURS.put('a', DyeColor.PINK);
        PMC_COLOURS.put('b', DyeColor.LIME);
        PMC_COLOURS.put('c', DyeColor.YELLOW);
        PMC_COLOURS.put('d', DyeColor.LIGHT_BLUE);
        PMC_COLOURS.put('e', DyeColor.MAGENTA);
        PMC_COLOURS.put('f', DyeColor.ORANGE);
        PMC_COLOURS.put('g', DyeColor.WHITE);
    }
    private static final BiMap<Character, ResourceKey<BannerPattern>> PMC_PATTERNS = HashBiMap.create(40);
    static {
        PMC_PATTERNS.put('o', BannerPatterns.STRIPE_BOTTOM);
        PMC_PATTERNS.put('v', BannerPatterns.STRIPE_TOP);
        PMC_PATTERNS.put('s', BannerPatterns.STRIPE_LEFT);
        PMC_PATTERNS.put('u', BannerPatterns.STRIPE_RIGHT);
        PMC_PATTERNS.put('p', BannerPatterns.STRIPE_CENTER);
        PMC_PATTERNS.put('t', BannerPatterns.STRIPE_MIDDLE);
        PMC_PATTERNS.put('r', BannerPatterns.STRIPE_DOWNRIGHT);
        PMC_PATTERNS.put('q', BannerPatterns.STRIPE_DOWNLEFT);
        PMC_PATTERNS.put('i', BannerPatterns.STRIPE_SMALL);
        PMC_PATTERNS.put('n', BannerPatterns.STRAIGHT_CROSS);
        PMC_PATTERNS.put('7', BannerPatterns.CROSS);
        PMC_PATTERNS.put('a', BannerPatterns.DIAGONAL_RIGHT_MIRROR);
        PMC_PATTERNS.put('9', BannerPatterns.DIAGONAL_LEFT);
        PMC_PATTERNS.put('A', BannerPatterns.DIAGONAL_LEFT_MIRROR);
        PMC_PATTERNS.put('B', BannerPatterns.DIAGONAL_RIGHT);
        PMC_PATTERNS.put('e', BannerPatterns.HALF_VERTICAL);
        PMC_PATTERNS.put('E', BannerPatterns.HALF_VERTICAL_MIRROR);
        PMC_PATTERNS.put('d', BannerPatterns.HALF_HORIZONTAL);
        PMC_PATTERNS.put('D', BannerPatterns.HALF_HORIZONTAL_MIRROR);
        PMC_PATTERNS.put('j', BannerPatterns.SQUARE_BOTTOM_LEFT);
        PMC_PATTERNS.put('k', BannerPatterns.SQUARE_BOTTOM_RIGHT);
        PMC_PATTERNS.put('l', BannerPatterns.SQUARE_TOP_LEFT);
        PMC_PATTERNS.put('m', BannerPatterns.SQUARE_TOP_RIGHT);
        PMC_PATTERNS.put('y', BannerPatterns.TRIANGLE_BOTTOM);
        PMC_PATTERNS.put('z', BannerPatterns.TRIANGLE_TOP);
        PMC_PATTERNS.put('w', BannerPatterns.TRIANGLES_BOTTOM);
        PMC_PATTERNS.put('x', BannerPatterns.TRIANGLES_TOP);
        PMC_PATTERNS.put('5', BannerPatterns.CIRCLE_MIDDLE);
        PMC_PATTERNS.put('g', BannerPatterns.RHOMBUS_MIDDLE);
        PMC_PATTERNS.put('3', BannerPatterns.BORDER);
        PMC_PATTERNS.put('8', BannerPatterns.CURLY_BORDER);
        PMC_PATTERNS.put('4', BannerPatterns.BRICKS);
        PMC_PATTERNS.put('6', BannerPatterns.CREEPER);
        PMC_PATTERNS.put('h', BannerPatterns.SKULL);
        PMC_PATTERNS.put('b', BannerPatterns.FLOWER);
        PMC_PATTERNS.put('f', BannerPatterns.MOJANG);
        PMC_PATTERNS.put('F', BannerPatterns.GLOBE);
        PMC_PATTERNS.put('G', BannerPatterns.PIGLIN);
        PMC_PATTERNS.put('c', BannerPatterns.GRADIENT);
        PMC_PATTERNS.put('C', BannerPatterns.GRADIENT_UP);
    }

    public static final ItemStack JSST_BANNER = builder(DyeColor.BLACK)
            .add(BannerPatterns.GRADIENT, DyeColor.GRAY)
            .add(BannerPatterns.CIRCLE_MIDDLE, DyeColor.WHITE)
            .add(BannerPatterns.FLOWER, DyeColor.MAGENTA)
            .build();
    public static final ItemStack JSST_SHIELD = builder(DyeColor.BLACK)
            .add(BannerPatterns.GRADIENT, DyeColor.GRAY)
            .add(BannerPatterns.CIRCLE_MIDDLE, DyeColor.WHITE)
            .add(BannerPatterns.FLOWER, DyeColor.MAGENTA)
            .setShield(true)
            .build();
    public static final ItemStack PMC_BANNER = builder(DyeColor.BLUE)
            .add(BannerPatterns.GRADIENT, DyeColor.BLUE)
            .add(BannerPatterns.GLOBE, DyeColor.LIME)
            .build();
    public static final ItemStack PMC_LINK_BANNER = builder(DyeColor.LIME)
            .add(BannerPatterns.GRADIENT, DyeColor.GREEN)
            .add(BannerPatterns.GLOBE, DyeColor.BLUE)
            .build();

    public static List<BannerPattern> getPatterns() {
        return BuiltInRegistries.BANNER_PATTERN.stream().filter(pattern -> !pattern.getHashname().equals("b")).collect(Collectors.toList());
    }

    public static String toPMCCode(DyeColor baseColour, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        if (baseColour == null) return "g";
        var builder = new StringBuilder(1 + patterns.size() * 2);
        builder.append(PMC_COLOURS.inverse().get(baseColour));
        for (Pair<Holder<BannerPattern>, DyeColor> pattern : patterns) {
            var resourceKey = BuiltInRegistries.BANNER_PATTERN.getResourceKey(pattern.getFirst().value());
            if (resourceKey.isPresent()) {
                builder.append(PMC_COLOURS.inverse().get(pattern.getSecond()));
                builder.append(PMC_PATTERNS.inverse().get(resourceKey.get()));
            }
        }
        return builder.toString();
    }

    public static Component getName(Pair<Holder<BannerPattern>, DyeColor> pattern) {
        var resourceKey = BuiltInRegistries.BANNER_PATTERN.getResourceKey(pattern.getFirst().value());
        return resourceKey.map(bannerPatternResourceKey -> Component.translatable("block.minecraft.banner." + bannerPatternResourceKey.location()
                        .toShortLanguageKey() + "." + pattern.getSecond().getName()))
                .orElseGet(() -> Component.literal("unknown"));
    }

    // Translates a PMC banner code, changing colours in `translation`
    public static String colourSwapPMC(String from, Map<DyeColor, DyeColor> translation) {
        var builder = new StringBuilder();
        for (int i = 0; i < from.length(); i++) {
            if (i > 0 && i % 2 == 0) {
                builder.append(from.charAt(i));
            } else {
                var colour = PMC_COLOURS.get(from.charAt(i));
                var translated = translation.getOrDefault(colour, colour);
                builder.append(PMC_COLOURS.inverse().get(translated));
            }
        }
        return builder.toString();
    }

    @Nullable
    public static ItemStack fromPMCCode(String code) {
        if ((code.length() & 1) == 0) return null;
        var baseColour = PMC_COLOURS.get(code.charAt(0));
        if (baseColour == null) return null;
        var builder = builder(baseColour);
        for (int i = 1; i < code.length(); i += 2) {
            var colour = PMC_COLOURS.get(code.charAt(i));
            var pattern = PMC_PATTERNS.get(code.charAt(i + 1));
            if (colour == null || pattern == null) continue;
            builder.add(pattern, colour);
        }
        return builder.build();
    }

    public static Builder builder(DyeColor base) {
        return new Builder(base);
    }

    public static class Builder {
        public static final Map<DyeColor, Item> BY_COLOUR = new LinkedHashMap<>();
        static {
            BY_COLOUR.put(DyeColor.WHITE, Items.WHITE_BANNER);
            BY_COLOUR.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_BANNER);
            BY_COLOUR.put(DyeColor.GRAY, Items.GRAY_BANNER);
            BY_COLOUR.put(DyeColor.BLACK, Items.BLACK_BANNER);
            BY_COLOUR.put(DyeColor.BROWN, Items.BROWN_BANNER);
            BY_COLOUR.put(DyeColor.RED, Items.RED_BANNER);
            BY_COLOUR.put(DyeColor.ORANGE, Items.ORANGE_BANNER);
            BY_COLOUR.put(DyeColor.YELLOW, Items.YELLOW_BANNER);
            BY_COLOUR.put(DyeColor.LIME, Items.LIME_BANNER);
            BY_COLOUR.put(DyeColor.GREEN, Items.GREEN_BANNER);
            BY_COLOUR.put(DyeColor.CYAN, Items.CYAN_BANNER);
            BY_COLOUR.put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_BANNER);
            BY_COLOUR.put(DyeColor.BLUE, Items.BLUE_BANNER);
            BY_COLOUR.put(DyeColor.PURPLE, Items.PURPLE_BANNER);
            BY_COLOUR.put(DyeColor.MAGENTA, Items.MAGENTA_BANNER);
            BY_COLOUR.put(DyeColor.PINK, Items.PINK_BANNER);
        }
        public static final Map<DyeColor, ItemStack> ICONS = Builder.BY_COLOUR.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), Labels.create(entry.getValue()).withName(ColourMenu.colourName(entry.getKey().getName()).copy().withStyle(CommandUtils.CLEAN)).build()))
                .collect(EditorUtils.linkedMapCollector(Pair::getFirst, Pair::getSecond));
        private final DyeColor colour;
        private List<Pair<Holder<BannerPattern>, DyeColor>> patterns = new ArrayList<>();
        private boolean shield = false;
        @Nullable
        private CompoundTag mergeTag;

        private Builder(DyeColor base) {
            this.colour = base;
        }

        public Builder setShield(boolean isShield) {
            this.shield = isShield;
            return this;
        }

        public Builder mergeTag(CompoundTag tag) {
            this.mergeTag = tag;
            return this;
        }

        public Builder set(List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder add(Holder<BannerPattern> pattern, DyeColor colour) {
            return add(Pair.of(pattern, colour));
        }

        public Builder add(Pair<Holder<BannerPattern>, DyeColor> pair) {
            this.patterns.add(pair);
            return this;
        }

        public Builder add(ResourceKey<BannerPattern> pattern, DyeColor colour) {
            return add(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(pattern), colour);
        }

        public ItemStack build() {
            var stack = new ItemStack(shield ? Items.SHIELD : BY_COLOUR.get(colour));
            var patternBuilder = new BannerPattern.Builder();
            if (mergeTag != null) stack.setTag(mergeTag);
            var beTag = new CompoundTag();
            this.patterns.forEach(patternBuilder::addPattern);
            beTag.put("Patterns", patternBuilder.toListTag());
            if (shield) beTag.putInt("Base", colour.getId());
            BlockItem.setBlockEntityData(stack, BlockEntityType.BANNER, beTag);
            return stack;
        }
    }
}
