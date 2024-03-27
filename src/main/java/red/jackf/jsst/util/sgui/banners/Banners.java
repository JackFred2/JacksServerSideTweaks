package red.jackf.jsst.util.sgui.banners;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;

public class Banners {
    private Banners() {}

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

    public interface Misc {
        ItemStack ERROR = fromPMCCode("1e718");

        ItemStack JSST = fromPMCCode("6adei6cgbgbeb");
    }

    public interface Arrows {
        ItemStack EMPTY = fromPMCCode("1d5");
        ItemStack UP = fromPMCCode("12v181g13");
        ItemStack DOWN = fromPMCCode("1bo181g13");
        ItemStack VERTICAL = fromPMCCode("1bo2v181g13");
        ItemStack LEFT = fromPMCCode("12g1E");
        ItemStack RIGHT = fromPMCCode("1bg1e");
        ItemStack HORIZONTAL = fromPMCCode("1bu2s1o1v18");
    }

    public static MutableComponent name(Holder<BannerPattern> pattern, DyeColor colour) {
        return Component.translatable(pattern.unwrapKey()
                .map(key -> "block.minecraft.banner." + key.location().toShortLanguageKey() + "." + colour.getName())
                .orElse("jsst.itemEditor.banner.unknownPattern"));
    }

    public static ItemStack fromColours(DyeColor top, DyeColor bottom) {
        return builder(top)
                .add(net.minecraft.world.level.block.entity.BannerPatterns.GRADIENT_UP, bottom)
                .build(false);
    }

    public static ItemStack fromPMCCode(String pmcCode) {
        return fromPMCCode(pmcCode, false);
    }

    public static ItemStack fromPMCCode(String pmcCode, boolean keepLore) {
        if (pmcCode.length() % 2 == 0) return Misc.ERROR.copy();
        char[] chars = pmcCode.toCharArray();
        Builder builder = builder(PMC.COLOURS.getOrDefault(chars[0], DyeColor.WHITE));

        for (int i = 1; i < pmcCode.length(); i += 2) {
            var colour = PMC.COLOURS.get(chars[i]);
            var pattern = PMC.PATTERNS.get(chars[i + 1]);
            if (colour != null && pattern != null)
                builder.add(pattern, colour);
        }

        ItemStack result = builder.build(false);

        if (!keepLore) result.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);

        return result;
    }

    public static String toPMCCode(DyeColor baseColour, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        StringBuilder builder = new StringBuilder(1 + patterns.size() * 2);
        builder.append(PMC.COLOURS.inverse().get(baseColour));

        for (var pattern : patterns) {
            Optional<ResourceKey<BannerPattern>> key = pattern.getFirst().unwrapKey();
            if (key.isPresent()) {
                builder.append(PMC.COLOURS.inverse().get(pattern.getSecond()));
                builder.append(PMC.PATTERNS.inverse().get(key.get()));
            }
        }

        return builder.toString();
    }

    public static BannerPatterns parseStack(ItemStack stack) {
        DyeColor baseColour = DyeColor.WHITE;
        if (stack.getItem() instanceof BannerItem bannerItem) {
            baseColour = bannerItem.getColor();
        } else if (stack.is(Items.SHIELD)) {
            baseColour = ShieldItem.getColor(stack);
        }

        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = BannerBlockEntity.createPatterns(DyeColor.WHITE, BannerBlockEntity.getItemPatterns(stack));
        patterns.remove(0);

        return new BannerPatterns(baseColour, patterns);
    }

    public static Builder builder(DyeColor baseColour) {
        return new Builder(baseColour);
    }

    public static class Builder {
        private final DyeColor baseColour;
        private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = new ArrayList<>(16);

        private Builder(DyeColor baseColour) {
            this.baseColour = baseColour;
        }

        public Builder add(ResourceKey<BannerPattern> pattern, DyeColor colour) {
            this.patterns.add(Pair.of(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(pattern), colour));
            return this;
        }

        public Builder add(Holder<BannerPattern> pattern, DyeColor colour) {
            this.patterns.add(Pair.of(pattern, colour));
            return this;
        }

        public ItemStack build(boolean isShield) {
            ItemStack stack = new ItemStack(isShield ? Items.SHIELD : BY_COLOUR.get(baseColour));
            BannerPattern.Builder patternBuilder = new BannerPattern.Builder();
            CompoundTag beTag = new CompoundTag();
            this.patterns.forEach(pair -> patternBuilder.addPattern(pair.getFirst(), pair.getSecond()));
            beTag.put("Patterns", patternBuilder.toListTag());
            if (isShield) beTag.putInt("Base", baseColour.getId());
            BlockItem.setBlockEntityData(stack, BlockEntityType.BANNER, beTag);
            return stack;
        }
    }

    public record BannerPatterns(DyeColor baseColour, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {

    }
}
