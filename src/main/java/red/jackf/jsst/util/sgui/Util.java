package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.GradientBuilder;

import java.util.OptionalInt;

public interface Util {
    static void returnItems(ServerPlayer player, Container container) {
        if (!player.isAlive() || player.hasDisconnected()) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
        } else {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }
        }
    }

    static int slot(int column, int row) {
        return row * 9 + column;
    }

    /**
     * Translates a given index to a raw slot number in a 'rectangle'
     * @param colFrom Start column, inclusive
     * @param colTo End column, exclusive
     * @param rowFrom Start row, inclusive
     * @param rowTo End row, exclusive
     */
    static SlotTranslator slotTranslator(int colFrom, int colTo, int rowFrom, int rowTo) {
        final int width = colTo - colFrom;
        final int height = rowTo - rowFrom;
        return new SlotTranslator(colFrom, rowFrom, width, height);
    }

    static void fill(SlotHolder holder, ItemStack stack, int colFrom, int colTo, int rowFrom, int rowTo) {
        for (int col = colFrom; col < colTo; col++) {
            for (int row = rowFrom; row < rowTo; row++) {
                holder.setSlot(slot(col, row), stack);
            }
        }
    }

    static Component getLabelAsTooltip(ItemStack stack) {
        Style style = Style.EMPTY.withColor(stack.getRarity().color)
                .withItalic(stack.hasCustomHoverName());
        return Component.empty().append(stack.getHoverName()).withStyle(style);
    }

    static void setName(GuiElementBuilderInterface<?> builder, Component name) {
        if (builder instanceof AnimatedGuiElementBuilder anim) anim.setName(name);
        else if (builder instanceof GuiElementBuilder stat) stat.setName(name);
        else throw new IllegalArgumentException("Unknown element builder");
    }

    static void addLore(GuiElementBuilderInterface<?> builder, Component lore) {
        if (builder instanceof AnimatedGuiElementBuilder anim) anim.addLoreLine(lore);
        else if (builder instanceof GuiElementBuilder stat) stat.addLoreLine(lore);
        else throw new IllegalArgumentException("Unknown element builder");
    }

    static Component colourise(Component text, MutableComponent base, Gradient gradient) {
        String str = text.getString();
        final int divisor = str.length() - 1;
        for (int i = 0; i < str.length(); i++) {
            float progress = (float) i / divisor;
            if (progress == 1F) progress = GradientBuilder.END;
            base.append(Component.literal(String.valueOf(str.charAt(i))).withColor(gradient.sample(progress).toARGB()));
        }
        return base;
    }

    interface Enums {
        static <E extends Enum<E>> E next(E current) {
            E[] constants = current.getDeclaringClass().getEnumConstants();
            return constants[(current.ordinal() + 1) % constants.length];
        }

        static <E extends Enum<E>> E previous(E current) {
            E[] constants = current.getDeclaringClass().getEnumConstants();
            return constants[current.ordinal() == 0 ? constants.length - 1 : current.ordinal() - 1];
        }
    }

    final class SlotTranslator {
        private final int colFrom;
        private final int rowFrom;
        private final int width;
        private final int height;

        public SlotTranslator(int colFrom, int rowFrom, int width, int height) {
            this.colFrom = colFrom;
            this.rowFrom = rowFrom;
            this.width = width;
            this.height = height;
        }

        public boolean outOfRange(int index) {
            return index < 0 || index >= width * height;
        }

        public OptionalInt translate(int index) {
            if (outOfRange(index)) return OptionalInt.empty();
            int row = index / width + rowFrom;
            int column = index % width + colFrom;
            return OptionalInt.of(row * 9 + column);
        }
    }
}
