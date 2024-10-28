package red.jackf.jsst.impl.utils.sgui.elements;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.Arguments;
import red.jackf.jsst.impl.utils.Cycling;
import red.jackf.jsst.impl.utils.TextUtils;
import red.jackf.jsst.impl.utils.sgui.Hints;
import red.jackf.jsst.impl.utils.sgui.Inputs;
import red.jackf.jsst.impl.utils.sgui.Styles;

import java.util.*;
import java.util.function.Consumer;

/**
 * Cycles between a set of options
 */
public class CycleButton<T> implements GuiElementInterface {
    private final Component label;
    private final List<T> options;
    private final Map<T, GuiElementInterface> labels;
    private final Consumer<T> changeCallback;

    private T current;

    public CycleButton(Component label, List<T> options, Map<T, GuiElementInterface> labels, T initial, Consumer<T> changeCallback) {
        this.label = label;
        this.options = options;
        this.labels = labels;
        this.current = initial;
        this.changeCallback = changeCallback;

        this.changeCallback.accept(current);
    }

    private ItemStack prepStackLabel(ItemStack stack) {
        var builder = JSSTElementBuilder.from(stack)
                .setName(this.label);

        for (T option : this.options) {
            Component title = TextUtils.copyNoStyle(this.labels.get(option).getItemStack().getHoverName());

            if (option == this.current) {
                builder.addLoreLine(Component.literal(" > ").withStyle(Styles.POSITIVE).append(title).append(" <"));
            } else {
                builder.addLoreLine(Component.literal(" ").withStyle(Styles.MINOR_LABEL).append(title));
            }
        }

        return builder.addLoreLine(Hints.leftClick(Component.translatable("jsst.ui.next")))
                .addLoreLine(Hints.rightClick(Component.translatable("jsst.ui.previous")))
                .asStack();
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack current = this.labels.get(this.current).getItemStack();

        return prepStackLabel(current);
    }

    @Override
    public ClickCallback getGuiCallback() {
        return Inputs.leftClick(() -> {
            this.current = Cycling.next(this.options, this.current);
            this.changeCallback.accept(this.current);
        }, Inputs.rightClick(() -> {
            this.current = Cycling.previous(this.options, this.current);
            this.changeCallback.accept(this.current);
        }));
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        ItemStack current = this.labels.get(this.current).getItemStackForDisplay(gui);

        return prepStackLabel(current);
    }

    public static <T> Builder<T> builder(Component name) {
        return new Builder<>(name);
    }

    public static class Builder<T> {
        private final Component name;
        private final List<T> options = new ArrayList<>();
        private final Map<T, GuiElementInterface> labels = new HashMap<>();
        private T initial = null;

        public Builder(Component name) {
            this.name = name;
        }

        public Builder<T> option(T option, GuiElementInterface label) {
            this.options.add(option);
            this.labels.put(option, label);
            if (this.initial == null) this.initial = option;
            return this;
        }

        public Builder<T> initial(T initial) {
            this.initial = initial;
            return this;
        }

        public CycleButton<T> build(Consumer<T> changeCallback) {
            Arguments.isGreaterOrEq(this.options.size(), 1, "Not enough options");
            return new CycleButton<>(name, options, labels, initial, changeCallback);
        }
    }
}
