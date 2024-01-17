package red.jackf.jsst.util.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Inputs;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;

import java.util.Objects;
import java.util.function.Consumer;

public class ToggleButton implements GuiElementInterface {
    private final Component label;
    private final GuiElementInterface disabled;
    private final GuiElementInterface enabled;
    private final Consumer<Boolean> callback;

    private final boolean makeEnabledGlow;
    private boolean value;

    private ToggleButton(
            Component label,
            GuiElementInterface disabled,
            GuiElementInterface enabled,
            boolean makeEnabledGlow,
            boolean initial,
            Consumer<Boolean> callback) {
        this.label = label;
        this.disabled = disabled;
        this.enabled = enabled;
        this.makeEnabledGlow = makeEnabledGlow;
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

        JSSTElementBuilder builder;
        if (this.value) {
            builder = JSSTElementBuilder.from(enabled)
                                       .setName(this.label.copy().withStyle(Styles.POSITIVE));
            if (this.makeEnabledGlow) builder.glow();
        } else {
            builder = JSSTElementBuilder.from(disabled)
                                       .setName(this.label.copy().withStyle(Styles.NEGATIVE));
        }

        return builder.addLoreLine(Hints.leftClick(Translations.toggle())).asStack();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Component label;
        private GuiElementInterface disabled = JSSTElementBuilder.from(Items.RED_CONCRETE).build();
        private GuiElementInterface enabled = JSSTElementBuilder.from(Items.LIME_CONCRETE).build();
        private Consumer<Boolean> callback;
        private boolean makeEnabledGlow = false;
        private boolean initial = false;

        private Builder() {}

        public Builder label(Component label) {
            this.label = label;
            return this;
        }

        public Builder disabled(GuiElementInterface disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder disabled(GuiElementBuilderInterface<?> disabled) {
            this.disabled = disabled.build();
            return this;
        }

        public Builder disabled(ItemStack disabled) {
            this.disabled = JSSTElementBuilder.from(disabled).build();
            return this;
        }

        public Builder enabled(GuiElementInterface enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder enabled(GuiElementBuilderInterface<?> enabled) {
            this.enabled = enabled.build();
            return this;
        }

        public Builder enabled(ItemStack enabled) {
            this.enabled = JSSTElementBuilder.from(enabled).build();
            return this;
        }

        public Builder makeEnabledGlow() {
            this.makeEnabledGlow = true;
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
            Objects.requireNonNull(label);
            Objects.requireNonNull(callback);
            return new ToggleButton(label, disabled, enabled, makeEnabledGlow, initial, callback);
        }
    }
}
