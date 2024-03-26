package red.jackf.jsst.util.sgui.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.entity.BannerPattern;
import red.jackf.jackfredlib.api.base.ServerTracker;
import red.jackf.jsst.feature.itemeditor.gui.editors.PotionEditor;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.banners.Banners;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public abstract class LabelMaps {
    public static final LabelMap<Item> ITEMS;
    public static final LabelMap<Item> ITEMS_WITH_SUSPICIOUS_STEW_EFFECTS;
    public static final LabelMap<DyeColor> DYES;
    public static final Function<DyeColor, LabelMap<Holder<BannerPattern>>> BANNER_PATTERNS;
    public static final LabelMap<Enchantment> ENCHANTMENTS;
    public static final LabelMap<MobEffect> MOB_EFFECTS;
    public static final LabelMap<Potion> POTIONS;
    public static final LabelMap<Holder<TrimMaterial>> TRIM_MATERIALS;
    public static final LabelMap<Holder<TrimPattern>> TRIM_PATTERNS;
    public static final LabelMap<Integer> BOOK_GENERATIONS;
    public static final LabelMap<Attribute> ATTRIBUTES;

    static {
        ITEMS = LabelMap.createStatic(Collections.emptyMap(), Item::getDefaultInstance);

        ITEMS_WITH_SUSPICIOUS_STEW_EFFECTS = ITEMS.withAdditional((item, map) -> {
            var holder = SuspiciousEffectHolder.tryGet(item);
            if (holder == null) return null;
            var builder = JSSTElementBuilder.from(map.getLabel(item));

            var server = ServerTracker.INSTANCE.getServer();

            for (SuspiciousEffectHolder.EffectEntry effect : holder.getSuspiciousEffects()) {
                builder.addLoreLine(PotionEditor.describe(effect.createEffectInstance(), server != null ? server.tickRateManager()
                        .tickrate() : SharedConstants.TICKS_PER_SECOND));
            }

            return builder.asStack();
        });

        DYES = LabelMap.createStatic(Collections.emptyMap(), colour -> {
            ItemStack stack = DyeItem.byColor(colour).getDefaultInstance();
            stack.setHoverName(Translations.dye(colour));
            return stack;
        });

        BANNER_PATTERNS = colour -> {
            DyeColor base = colour == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE;
            return pattern -> JSSTElementBuilder.ui(Banners.builder(base)
                            .add(pattern, colour)
                            .build(false))
                    .setName(Banners.name(pattern, colour))
                    .hideFlags()
                    .asStack();
        };

        ENCHANTMENTS = LabelMap.createDataManaged(BuiltInRegistries.ENCHANTMENT, ench -> {
            ItemStack stack = Items.ENCHANTED_BOOK.getDefaultInstance();
            EnchantmentHelper.setEnchantments(Map.of(ench, ench.getMaxLevel()), stack);
            return stack;
        }, ench -> Component.translatable(ench.getDescriptionId())
                .withStyle(ench.isCurse() ? ChatFormatting.RED : ChatFormatting.GRAY));

        MOB_EFFECTS = LabelMap.createDataManaged(BuiltInRegistries.MOB_EFFECT,
                effect -> Items.POTION.getDefaultInstance(),
                MobEffect::getDisplayName);

        POTIONS = LabelMap.createStatic(Map.of(
                Potions.EMPTY,
                Items.GLASS_BOTTLE.getDefaultInstance()
                        .setHoverName(Component.translatable("jsst.itemEditor.potion.noPotion"))
        ), potion -> {
            var stack = Items.POTION.getDefaultInstance();
            PotionUtils.setPotion(stack, potion);
            stack.setHoverName(Component.translatable(potion.getName(Items.POTION.getDescriptionId() + ".effect.")));
            return stack;
        });

        TRIM_MATERIALS = LabelMap.createStatic(Collections.emptyMap(), material -> JSSTElementBuilder.from(material.value()
                        .ingredient().value())
                .setName(material.value().description())
                .hideFlags()
                .asStack());

        TRIM_PATTERNS = LabelMap.createStatic(Collections.emptyMap(), pattern -> JSSTElementBuilder.from(pattern.value()
                        .templateItem().value())
                .setName(pattern.value().description())
                .hideFlags()
                .asStack());

        BOOK_GENERATIONS = i -> {
            i = Mth.clamp(i, 0, 3);
            var stack = JSSTElementBuilder.ui(Items.WRITTEN_BOOK)
                    .setName(Component.translatable("book.generation." + i))
                    .setCount(i + 1)
                    .hideFlags()
                    .glow()
                    .asStack();
            stack.getOrCreateTag().putInt(WrittenBookItem.TAG_GENERATION, i);
            return stack;
        };

        ATTRIBUTES = LabelMap.createDataManaged(BuiltInRegistries.ATTRIBUTE,
                attribute -> Items.BOOK.getDefaultInstance(),
                attribute -> Component.translatable(attribute.getDescriptionId()));
    }

    public static void touch() {
    }
}
