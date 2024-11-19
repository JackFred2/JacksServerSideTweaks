package red.jackf.jsst.impl.utils.sgui.labels;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

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

    static void touch() {
        // no-op
    }
}
