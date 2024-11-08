package red.jackf.jsst.impl.utils.sgui.labels;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
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

    static void touch() {
        // no-op
    }
}
