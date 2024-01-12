package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.OptionalInt;

/**
 * Translates a rectangle for a given GUI to their actual slots.
 */
public final class GridTranslator {
    private final int offset;
    private final int colFrom;
    private final int rowFrom;
    private final int width;
    private final int height;

    private GridTranslator(int offset, int colFrom, int rowFrom, int width, int height) {
        this.offset = offset;
        this.colFrom = colFrom;
        this.rowFrom = rowFrom;
        this.width = width;
        this.height = height;
    }

    public static GridTranslator between(int colFrom, int colTo, int rowFrom, int rowTo) {
        final int width = colTo - colFrom;
        final int height = rowTo - rowFrom;
        return new GridTranslator(0, colFrom, rowFrom, width, height);
    }

    public static GridTranslator playerSlots(SlotGuiInterface gui, int colFrom, int colTo, int rowFrom, int rowTo) {
        final int width = colTo - colFrom;
        final int height = rowTo - rowFrom;
        return new GridTranslator(gui.getVirtualSize(), colFrom, rowFrom, width, height);
    }

    public int size() {
        return width * height;
    }

    public boolean outOfRange(int index) {
        return index < 0 || index >= size();
    }

    public Iterable<Integer> slots() {
        var list = new ArrayList<Integer>();
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                list.add(offset + colFrom + col + 9 * (row + rowFrom));
            }
        }
        return list;
    }

    public OptionalInt translate(int index) {
        if (outOfRange(index)) return OptionalInt.empty();
        int row = index / width + rowFrom;
        int column = index % width + colFrom;
        return OptionalInt.of(offset + row * 9 + column);
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

    public <T> Iterable<SlotItemPair<T>> iterate(T[] elements) {
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

    public record SlotItemPair<T>(int slot, T item) {
    }
}
