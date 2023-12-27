package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;

public interface CommonLabels {
    static GuiElementInterface close(Runnable closeCallback) {
        return GuiElementBuilder.from(new ItemStack(Items.BARRIER))
                .setName(Translations.close())
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(closeCallback))
                .build();
    }

    static GuiElementInterface divider() {
        return GuiElementBuilder.from(new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE))
                                .setName(CommonComponents.EMPTY)
                                .build();
    }

    static GuiElementInterface disabled(Component text) {
        return GuiElementBuilder.from(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                                .setName(text)
                                .build();
    }

    static ItemStack simple(Item item, Component name, Component... lore) {
        return simple(new ItemStack(item), name, lore);
    }

    static ItemStack simple(ItemStack stack, Component name, Component... lore) {
        return GuiElementBuilder.from(stack)
                                .hideFlags()
                                .setName(name)
                                .setLore(Arrays.asList(lore))
                                .asStack();
    }
}
