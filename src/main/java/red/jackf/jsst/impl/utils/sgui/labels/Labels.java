package red.jackf.jsst.impl.utils.sgui.labels;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

public interface Labels {
    static ItemStack enchantment(Holder<Enchantment> enchantment) {
        var ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        ench.set(enchantment, enchantment.value().getMaxLevel());

        return JSSTElementBuilder.from(Items.ENCHANTED_BOOK)
                .setComponent(DataComponents.ENCHANTMENTS, ench.toImmutable().withTooltip(false))
                .setName(enchantment.value().description().copy())
                .hideDefaultTooltip()
                .asStack();
    }
}
