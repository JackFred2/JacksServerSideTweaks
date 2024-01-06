package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.SlotHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.GradientBuilder;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;

import java.util.ArrayList;
import java.util.List;
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

    static void clear(SlotHolder holder, int colFrom, int colTo, int rowFrom, int rowTo) {
        fill(holder, ItemStack.EMPTY, colFrom, colTo, rowFrom, rowTo);
    }

    static void fill(SlotHolder holder, ItemStack stack, int colFrom, int colTo, int rowFrom, int rowTo) {
        for (int col = colFrom; col < colTo; col++) {
            for (int row = rowFrom; row < rowTo; row++) {
                holder.setSlot(slot(col, row), stack);
            }
        }
    }

    static void unhideTooltipPart(ItemStack stack, ItemStack.TooltipPart... parts) {
        int mask = ((ItemStackAccessor) (Object) stack).jsst$itemEditor$getTooltipHideMask();
        for (ItemStack.TooltipPart part : parts) {
            mask &= ~part.getMask();
        }
        if (mask == 0) {
            stack.removeTagKey("HideFlags");
        } else {
            stack.getOrCreateTag().putInt("HideFlags", mask);
        }
    }

    static Component getLabelAsTooltip(ItemStack stack) {
        Style style = Style.EMPTY.withColor(stack.getRarity().color)
                .withItalic(stack.hasCustomHoverName());
        return Component.empty().append(stack.getHoverName()).withStyle(style);
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

    static Component formatAsHex(Colour colour) {
        return Component.literal(String.format("#%06X", colour.toARGB() & 0xFFFFFF))
                .setStyle(Styles.MINOR_LABEL);
    }

    static void sendAnvilCost(ServerPlayer player, int containerId, int cost) {
        player.connection.send(new ClientboundContainerSetDataPacket(
                containerId,
                0,
                cost
        ));
    }

    interface Lists {
        static <T> T next(T current, List<T> options) {
            int currentIndex = options.indexOf(current);
            if (currentIndex == -1) throw new IllegalArgumentException("Unknown option");
            if (currentIndex == options.size() - 1) return options.get(0);
            else return options.get(currentIndex + 1);
        }

        static <T> T previous(T current, List<T> options) {
            int currentIndex = options.indexOf(current);
            if (currentIndex == -1) throw new IllegalArgumentException("Unknown option");
            if (currentIndex == 0) return options.get(options.size() - 1);
            else return options.get(currentIndex - 1);
        }

        static <T> void swap(List<T> list, int indexA, int indexB) {
            list.set(indexA, list.set(indexB, list.get(indexA)));
        }
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

        public Iterable<Integer> slots() {
            var list = new ArrayList<Integer>();
            for (int col = 0; col < width; col++) {
                for (int row = 0; row < height; row++) {
                    list.add(colFrom + col + 9 * (row + rowFrom));
                }
            }
            return list;
        }

        public OptionalInt translate(int index) {
            if (outOfRange(index)) return OptionalInt.empty();
            int row = index / width + rowFrom;
            int column = index % width + colFrom;
            return OptionalInt.of(row * 9 + column);
        }

        public <T> Iterable<SlotItemPair<T>> iterate(Iterable<T> elements) {
            int index = 0;
            var result = new ArrayList<SlotItemPair<T>>();
            for (T element : elements) {
                var slot = this.translate(index++);
                if (slot.isEmpty()) return result;
                result.add(new SlotItemPair<>(slot.getAsInt(), element));
            }
            return result;
        }

        public void fill(SlotHolder gui, ItemStack fillMaterial) {
            for (int slot : slots())
                gui.setSlot(slot, fillMaterial);
        }

        public record SlotItemPair<T>(int slot, T item) {}
    }
}
