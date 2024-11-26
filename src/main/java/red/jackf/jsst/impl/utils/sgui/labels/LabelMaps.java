package red.jackf.jsst.impl.utils.sgui.labels;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jsst.impl.utils.Banners;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.function.Function;

public interface LabelMaps {
    LabelMap<Holder<Enchantment>> ENCHANTMENT = LabelMap.createDatapacked(Registries.ENCHANTMENT, ench -> {
        ItemEnchantments.Mutable component = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        component.set(ench, ench.value().getMaxLevel());

        return JSSTElementBuilder.from(Items.ENCHANTED_BOOK)
                .setComponent(DataComponents.ENCHANTMENTS, component.toImmutable())
                .asStack();
    }, (ench, stack) -> JSSTElementBuilder.from(stack).ui()
            .setName(ench.value().description().copy())
            .asStack());

    LabelMap<Holder<MobEffect>> MOB_EFFECT = LabelMap.createDatapacked(Registries.MOB_EFFECT,
            effect -> JSSTElementBuilder.from(Items.POTION)
                    .setComponent(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(effect)))
                    .asStack(),
            (effect, stack) -> JSSTElementBuilder.from(stack).ui()
                    .hideDefaultTooltip()
                    .setName(effect.value().getDisplayName().copy())
                    .asStack());

    Function<DyeColor, LabelMap<Holder<BannerPattern>>> BANNER_PATTERN = colour -> option -> JSSTElementBuilder.from(Banners.ByColour.ITEM.get(ColourUtils.getContrasting(colour))).ui()
                .setName(Component.translatable(option.value().translationKey() + "." + colour.getName()))
                .setComponent(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                        .add(option, colour)
                        .build())
                .hideDefaultTooltip()
                .asStack();

    LabelMap<DyeColor> DYE_COLOR = colour -> JSSTElementBuilder.from(DyeItem.byColor(colour)).ui()
            .setName(Translations.colour(colour).withColor(Colour.fromInt(colour.getFireworkColor()).lerp(Colours.WHITE, 0.1f).toARGB()))
            .asStack();

    static void touch() {
        // no-op
    }
}
