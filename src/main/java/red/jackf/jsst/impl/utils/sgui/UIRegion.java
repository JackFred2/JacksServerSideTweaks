package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class UIRegion {
    private final SimpleGuiExt gui;
    private final List<Integer> slots;

    public static UIRegion single(SimpleGuiExt gui, int slot) {
        return new UIRegion(gui, List.of(slot));
    }

    public static UIRegion single(SimpleGuiExt gui, int column, int row) {
        return new UIRegion(gui, List.of(gui.getSlotFor(column, row)));
    }

    public static UIRegion list(SimpleGuiExt gui, Integer... slots) {
        return new UIRegion(gui, List.of(slots));
    }

    public static UIRegion rectangle(SimpleGuiExt gui, int startColumnInclusive, int startRowInclusive, int endColumnExclusive, int endRowExclusive) {
        Arguments.inRange(startColumnInclusive, 0, gui.getWidth(), "startColumn out of range: %d");
        Arguments.inRange(startRowInclusive, 0, gui.getHeight(), "startRow out of range: %d");
        Arguments.inRange(endColumnExclusive, 0, gui.getWidth(), "endColumn out of range: %d");
        Arguments.inRange(endRowExclusive, 0, gui.getHeight(), "startColumn out of range: %d");
        Arguments.isLessOrEq(startColumnInclusive, endColumnExclusive, "startColumn > endColumn: %d > %d");
        Arguments.isLessOrEq(startRowInclusive, endRowExclusive, "startRow > endRow: %d > %d");

        List<Integer> slots = new ArrayList<>();

        for (int col = startColumnInclusive; col < endColumnExclusive; col++) {
            for (int row = startRowInclusive; row < endRowExclusive; row++) {
                slots.add(gui.getSlotFor(col, row));
            }
        }

        return new UIRegion(gui, List.copyOf(slots));
    }

    public static UIRegion row(SimpleGuiExt gui, int row, int startColumnInclusive, int endColumnExclusive) {
        return rectangle(gui, startColumnInclusive, row, endColumnExclusive, row + 1);
    }

    public static UIRegion row(SimpleGuiExt gui, int row) {
        return row(gui, row, 0, gui.getWidth());
    }

    public static UIRegion column(SimpleGuiExt gui, int column, int startRowInclusive, int endRowExclusive) {
        return rectangle(gui, column, startRowInclusive, column + 1, endRowExclusive);
    }

    public static UIRegion column(SimpleGuiExt gui, int column) {
        return column(gui, column, 0, gui.getHeight());
    }

    private UIRegion(SimpleGuiExt gui, List<Integer> slots) {
        this.gui = gui;
        this.slots = slots;
    }

    public void clearSlots() {
        stream().forEach(this.gui::clearSlot);
    }

    public void fillStack(Supplier<ItemStack> stack) {
        stream().forEach(slot -> this.gui.setSlot(slot, stack.get()));
    }

    public void fillElement(Supplier<GuiElementInterface> stack) {
        stream().forEach(slot -> this.gui.setSlot(slot, stack.get()));
    }

    public void fillElementBuilder(Supplier<GuiElementBuilderInterface<?>> stack) {
        stream().forEach(slot -> this.gui.setSlot(slot, stack.get()));
    }

    public Stream<Integer> stream() {
        return this.slots.stream();
    }
}
