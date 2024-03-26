package red.jackf.jsst.util.sgui.elements;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SwitchButton<T> implements GuiElementInterface {
    private final Component name;
    private final List<T> options;
    private final Map<T, GuiElementInterface> labels;
    private final Consumer<T> callback;
    private T current;

    private SwitchButton(Component name, List<T> options, Map<T, GuiElementInterface> labels, Consumer<T> callback, T current) {
        this.name = name;
        this.options = options;
        this.labels = labels;
        this.callback = callback;
        this.current = current;
    }

    public static <T> Builder<T> builder(Component name) {
        return new Builder<>(name);
    }

    @Override
    public ItemStack getItemStack() {
        return this.labels.get(current).getItemStack();
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        List<Component> names = new ArrayList<>();
        ItemStack shown = ItemStack.EMPTY;
        for (Map.Entry<T, GuiElementInterface> entry : this.labels.entrySet()) {
            //ItemStack stack = entry.getValue().getItemStackForDisplay(gui); // tick all for animated
            ItemStack stack = Styles.unclean(entry.getValue().getItemStackForDisplay(gui).copy()); // tick all for animated
            if (entry.getKey() == current) {
                shown = stack;
                names.add(Component.empty().withStyle(Styles.POSITIVE).append(" - ").append(stack.getHoverName()));
            } else {
                names.add(Component.empty().withStyle(Styles.MINOR_LABEL).append(" - ").append(stack.getHoverName()));
            }
        }

        var builder = JSSTElementBuilder.from(shown).setName(this.name);

        names.forEach(builder::addLoreLine);

        return builder.addLoreLine(Hints.leftClick(Translations.next()))
                      .addLoreLine(Hints.rightClick(Translations.previous()))
                      .asStack();
    }

    @Override
    public ClickCallback getGuiCallback() {
        return this::click;
    }

    private void click(
            int slot,
            ClickType clickType,
            net.minecraft.world.inventory.ClickType mcClickType,
            SlotGuiInterface gui) {
        if (clickType == ClickType.MOUSE_LEFT) {
            Sounds.click(gui.getPlayer());
            this.current = Util.Lists.next(this.current, this.options);
            callback.accept(current);
        } else if (clickType == ClickType.MOUSE_RIGHT) {
            Sounds.click(gui.getPlayer());
            this.current = Util.Lists.previous(this.current, this.options);
            callback.accept(current);
        }
    }

    public static class Builder<T> {
        private final Component name;
        private final List<T> options = new ArrayList<>();
        private final Map<T, GuiElementInterface> labels = new LinkedHashMap<>();
        private Consumer<T> callback = t -> {};

        private Builder(Component name) {
            this.name = name;
        }

        public Builder<T> addOption(T option, GuiElementInterface label) {
            if (this.labels.containsKey(option)) throw new IllegalArgumentException("Duplicate element");
            this.options.add(option);
            this.labels.put(option, label);
            return this;
        }

        public Builder<T> addOption(T option, ItemStack label) {
            if (this.labels.containsKey(option)) throw new IllegalArgumentException("Duplicate element");
            this.options.add(option);
            this.labels.put(option, JSSTElementBuilder.from(label).build());
            return this;
        }

        public Builder<T> addOptions(T[] options, Function<T, ItemStack> labelGrabber) {
            for (T option : options) {
                addOption(option, labelGrabber.apply(option));
            }
            return this;
        }

        public Builder<T> setCallback(Consumer<T> callback) {
            this.callback = callback;
            return this;
        }

        public SwitchButton<T> build(T current) {
            if (options.isEmpty()) throw new IllegalArgumentException("No elements");
            if (!labels.containsKey(current)) throw new IllegalArgumentException("Unknown start element");
            return new SwitchButton<>(name, options, labels, callback, current);
        }
    }
}
