package red.jackf.jsst.util.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface CommonLabels {
    static GuiElementInterface close(Runnable closeCallback) {
        return GuiElementBuilder.from(new ItemStack(Items.BARRIER))
                .setName(Component.translatable("mco.selectServer.close"))
                .setCallback(closeCallback)
                .build();
    }

    static GuiElementInterface divider() {
        return GuiElementBuilder.from(new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE))
                .setName(CommonComponents.EMPTY)
                .build();
    }
}
