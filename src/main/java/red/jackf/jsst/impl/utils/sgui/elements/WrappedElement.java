package red.jackf.jsst.impl.utils.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WrappedElement<E extends GuiElementInterface> implements GuiElementInterface {
    private final E wrapped;
    private final List<Component> additionalLore;
    private final ClickCallback callback;

    private WrappedElement(E wrapped,
                          List<Component> additionalLore,
                          ClickCallback callback) {
        this.wrapped = wrapped;
        this.additionalLore = additionalLore;
        this.callback = callback;
    }

    private ItemStack buildStack(ItemStack in) {
        var builder = JSSTElementBuilder.from(in);
        this.additionalLore.forEach(builder::addLoreLine);
        return builder.asStack();
    }

    @Override
    public ItemStack getItemStack() {
        return buildStack(this.wrapped.getItemStack());
    }

    @Override
    public ClickCallback getGuiCallback() {
        return this.callback;
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return buildStack(this.wrapped.getItemStackForDisplay(gui));
    }

    public static <E extends GuiElementInterface> Builder<E> builder(E element) {
        return new Builder<>(element);
    }

    public static class Builder<E extends GuiElementInterface> implements GuiElementBuilderInterface<Builder<E>> {
        private final E wrapped;
        private final List<Component> additionalLore = new ArrayList<>();
        private ClickCallback callback = (a, b, c, d) -> {};

        private Builder(E wrapped) {
            this.wrapped = wrapped;
        }

        public Builder<E> addLore(Component lore) {
            this.additionalLore.add(lore);
            return this;
        }

        public Builder<E> addLore(List<Component> lore) {
            this.additionalLore.addAll(lore);
            return this;
        }

        public Builder<E> setAdditionalLore(List<Component> additionalLore) {
            this.additionalLore.clear();
            this.additionalLore.addAll(additionalLore);
            return this;
        }

        public Builder<E> setCallback(ClickCallback callback) {
            this.callback = callback;
            return this;
        }

        public WrappedElement<E> build() {
            return new WrappedElement<>(wrapped, additionalLore, callback);
        }
    }
}
