package red.jackf.jsst.util.sgui.labels;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import red.jackf.jackfredlib.api.base.ServerTracker;
import red.jackf.jsst.feature.itemeditor.gui.editors.PotionEditor;

import java.util.Collections;
import java.util.Map;

public interface LabelMaps {
    LabelMap<Item> ITEMS = LabelMap.createStatic(Collections.emptyMap(), ItemStack::new);
    LabelMap<MobEffect> MOB_EFFECTS = LabelMap.createDataManaged(BuiltInRegistries.MOB_EFFECT,
                                                                 effect -> Items.POTION.getDefaultInstance(),
                                                                 MobEffect::getDisplayName);
    LabelMap<Potion> POTIONS = LabelMap.createStatic(Map.of(
            Potions.EMPTY,
            Items.GLASS_BOTTLE.getDefaultInstance().setHoverName(Component.translatable("jsst.itemEditor.potion.noPotion"))
    ), potion -> {
        var stack = Items.POTION.getDefaultInstance();
        PotionUtils.setPotion(stack, potion);
        stack.setHoverName(Component.translatable(potion.getName(Items.POTION.getDescriptionId() + ".effect.")));
        return stack;
    });
    LabelMap<Holder<TrimMaterial>> TRIM_MATERIALS = LabelMap.createStatic(Collections.emptyMap(), material -> GuiElementBuilder.from(material.value().ingredient().value().getDefaultInstance())
            .setName(material.value().description())
            .hideFlags()
            .asStack());
    LabelMap<Holder<TrimPattern>> TRIM_PATTERNS = LabelMap.createStatic(Collections.emptyMap(), pattern -> GuiElementBuilder.from(pattern.value().templateItem().value().getDefaultInstance())
            .setName(pattern.value().description())
            .hideFlags()
            .asStack());
    LabelMap<Item> ITEMS_WITH_SUSPICIOUS_STEW_EFFECTS = ITEMS.withAdditional((item, map) -> {
        var holder = SuspiciousEffectHolder.tryGet(item);
        if (holder == null) return null;
        var builder = GuiElementBuilder.from(map.getLabel(item));

        var server = ServerTracker.INSTANCE.getServer();

        for (SuspiciousEffectHolder.EffectEntry effect : holder.getSuspiciousEffects()) {
            builder.addLoreLine(PotionEditor.describe(effect.createEffectInstance(), server != null ? server.tickRateManager().tickrate() : SharedConstants.TICKS_PER_SECOND));
        }

        return builder.asStack();
    });

    static void touch() {}
}
