package red.jackf.jsst.util.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Inputs;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;

import java.util.function.Consumer;

public class ToggleButton implements GuiElementInterface {
    private final Component label;
    private final GuiElementInterface disabled;
    private final GuiElementInterface enabled;
    private final Consumer<Boolean> callback;

    private boolean value;

    private ToggleButton(
            Component label,
            GuiElementInterface disabled,
            GuiElementInterface enabled,
            boolean initial,
            Consumer<Boolean> callback) {
        this.label = label;
        this.disabled = disabled;
        this.enabled = enabled;
        this.value = initial;
        this.callback = callback;
    }

    @Override
    public ItemStack getItemStack() {
        return this.value ? this.enabled.getItemStack() : this.disabled.getItemStack();
    }

    @Override
    public ClickCallback getGuiCallback() {
        return Inputs.leftClick(() -> this.callback.accept((this.value = !this.value)));
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        // get both to tick anim
        var disabled = this.disabled.getItemStackForDisplay(gui);
        var enabled = this.enabled.getItemStackForDisplay(gui);

        GuiElementBuilder builder;
        if (this.value) {
            builder = GuiElementBuilder.from(enabled)
                                       .setName(this.label.copy().withStyle(Styles.POSITIVE));
        } else {
            builder = GuiElementBuilder.from(disabled)
                                       .setName(this.label.copy().withStyle(Styles.NEGATIVE));
        }

        return builder.addLoreLine(Hints.leftClick(Translations.toggle())).asStack();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Component label;
        private GuiElementInterface disabled;
        private GuiElementInterface enabled;
        private Consumer<Boolean> callback;
        private boolean initial;

        private Builder() {}

        public Builder label(Component label) {
            this.label = label;
            return this;
        }

        public Builder disabled(GuiElementInterface disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder disabled(ItemStack disabled) {
            this.disabled = GuiElementBuilder.from(disabled).build();
            return this;
        }

        public Builder enabled(GuiElementInterface enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder enabled(ItemStack enabled) {
            this.enabled = GuiElementBuilder.from(enabled).build();
            return this;
        }

        public Builder initial(boolean initial) {
            this.initial = initial;
            return this;
        }

        public Builder setCallback(Consumer<Boolean> callback) {
            this.callback = callback;
            return this;
        }

        public ToggleButton build() {
            return new ToggleButton(label, disabled, enabled, initial, callback);
        }
    }
}
