package red.jackf.jsst.impl.utils.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.utils.sgui.Hints;
import red.jackf.jsst.impl.utils.sgui.Inputs;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.function.Consumer;

public class ToggleButton implements GuiElementInterface {
    private final Consumer<Boolean> callback;

    private final WrappedElement<GuiElementInterface> disabled;
    private final WrappedElement<GuiElementInterface> enabled;

    private boolean value;

    private ToggleButton(Component label, boolean initial, GuiElementInterface disabled, GuiElementInterface enabled, Consumer<Boolean> callback) {
        this.value = initial;
        this.callback = callback;

        this.disabled = WrappedElement.builder(disabled)
                .setName(Component.empty().withStyle(Styles.NEGATIVE).append(label))
                .addLore(Hints.leftClick(Translations.toggle()))
                .build();

        this.enabled = WrappedElement.builder(enabled)
                .setName(Component.empty().withStyle(Styles.POSITIVE).append(label))
                .addLore(Hints.leftClick(Translations.toggle()))
                .build();
    }

    @Override
    public ItemStack getItemStack() {
        return this.value ? this.enabled.getItemStack() : this.disabled.getItemStack();
    }

    @Override
    public ClickCallback getGuiCallback() {
        return Inputs.leftClick(() -> {
            this.value = !value;
            this.callback.accept(this.value);
        });
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return this.value ? this.enabled.getItemStackForDisplay(gui) : this.disabled.getItemStackForDisplay(gui);
    }

    public static Builder builder(Component label) {
        return new Builder(label);
    }

    public static class Builder {
        private final Component label;
        private boolean initialValue = false;
        private GuiElementInterface disabled = JSSTElementBuilder.from(Items.RED_CONCRETE).build();
        private GuiElementInterface enabled = JSSTElementBuilder.from(Items.LIME_CONCRETE).build();

        private Builder(Component label) {
            this.label = label;
        }

        public Builder enabled(GuiElementInterface enabledElement) {
            this.enabled = enabledElement;
            return this;
        }

        public Builder disabled(GuiElementInterface disabledElement) {
            this.disabled = disabledElement;
            return this;
        }

        public Builder initial(boolean initialValue) {
            this.initialValue = initialValue;
            return this;
        }

        public ToggleButton build(Consumer<Boolean> callback) {
            return new ToggleButton(label, initialValue, disabled, enabled, callback);
        }
    }
}
