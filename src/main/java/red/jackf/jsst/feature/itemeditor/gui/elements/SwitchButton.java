package red.jackf.jsst.feature.itemeditor.gui.elements;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;

import java.util.function.Consumer;
import java.util.function.Function;

public class SwitchButton {
    public static <E extends Enum<E>> void addOptionsAsLore(
            Class<E> clazz,
            E highlighted,
            Function<E, Component> optionNames,
            GuiElementBuilderInterface<?> builder) {
        for (E constant : clazz.getEnumConstants()) {
            Component line = Component.empty().withStyle(constant == highlighted ? Styles.POSITIVE : Styles.MINOR_LABEL)
                                      .append(" - ")
                                      .append(optionNames.apply(constant));
            Util.addLore(builder, line);
        }
    }

    public static <E extends Enum<E> & Labelled> GuiElementInterface create(
            Component title,
            Class<E> clazz,
            E current,
            Consumer<E> onChange) {
        GuiElementBuilderInterface<?> builder = current.getLabel(frame -> {
            Util.setName(frame, title);
            addOptionsAsLore(clazz, current, Labelled::getName, frame);

            Util.addLore(frame, Hints.leftClick(Translations.next()));
            Util.addLore(frame, Hints.rightClick(Translations.previous()));
        });

        builder.setCallback(type -> {
            if (type == ClickType.MOUSE_LEFT) {
                onChange.accept(Util.Enums.next(current));
            } else if (type == ClickType.MOUSE_RIGHT) {
                onChange.accept(Util.Enums.previous(current));
            }
        });

        return builder.build();
    }

    public interface Labelled {
        GuiElementBuilderInterface<?> getLabel(Consumer<GuiElementBuilderInterface<?>> applyToEachFrame);

        Component getName();
    }

    public interface LabelGetter {
        GuiElementBuilderInterface<?> get(Consumer<GuiElementBuilderInterface<?>> applyToEachFrame);
    }
}
