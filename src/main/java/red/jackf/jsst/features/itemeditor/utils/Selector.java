package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class Selector {
    public static <T extends Enum<T> & Labeled> ItemGuiElement create(Class<T> selector, String name, T current, Consumer<T> setter) {
        var constants = selector.getEnumConstants();
        var stack = Labels.create(current.label()).withName(name);
        for (T t : constants) {
            if (current == t) {
                stack.withHint(t.settingName());
            } else {
                stack.withHint(Component.literal(t.settingName()).withStyle(ChatFormatting.GRAY));
            }
        }
        return new ItemGuiElement(stack.build(), () -> {
            setter.accept(constants[(current.ordinal() + 1) % constants.length]);
        });
    }

    /**
     * Represents a set of options for this selector. The stack's name gets overridden.
     */
    public interface Labeled {
        ItemStack label();

        String settingName();
    }
}
