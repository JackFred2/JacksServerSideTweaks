package red.jackf.jsst.util.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WrappedElement implements GuiElementInterface {
    private final GuiElementInterface element;
    private final List<Component> additionalLore;
    private final ClickCallback callback;

    public WrappedElement(GuiElementInterface wrapped,
                          List<Component> additionalLore,
                          ClickCallback callback) {
        this.element = wrapped;
        this.additionalLore = additionalLore;
        this.callback = callback;
    }

    private ItemStack buildStack(ItemStack in) {
        var builder = GuiElementBuilder.from(in);
        this.additionalLore.forEach(builder::addLoreLine);
        return builder.asStack();
    }

    @Override
    public ItemStack getItemStack() {
        return buildStack(this.element.getItemStack());
    }

    @Override
    public ClickCallback getGuiCallback() {
        return callback;
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return buildStack(this.element.getItemStackForDisplay(gui));
    }
}
