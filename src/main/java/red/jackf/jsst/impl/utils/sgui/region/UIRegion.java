package red.jackf.jsst.impl.utils.sgui.region;

import com.google.common.collect.Streams;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Designates a set of slots for an SGUI UI. Contains methods for filling regions, populating with list elements.
 */
@SuppressWarnings("UnstableApiUsage")
public class UIRegion implements Iterable<Integer> {
    private final SimpleGuiExt gui;
    private final List<Integer> slots;

    protected UIRegion(SimpleGuiExt gui, List<Integer> slots) {
        this.gui = gui;
        this.slots = List.copyOf(slots);
    }

    /**
     * Creates a region designating a single slot.
     *
     * @param gui GUI this region is for.
     * @param slot Slot number of the gui with this region.
     * @return A region covering the singular slot.
     */
    public static UIRegion single(SimpleGuiExt gui, int slot) {
        return new UIRegion(gui, List.of(slot));
    }

    /**
     * Creates a region designating a single slot.
     *
     * @param gui GUI this region is for.
     * @param column Column of the slot.
     * @param row Row of the slot.
     * @return A region covering the singular slot.
     */
    public static UIRegion single(SimpleGuiExt gui, int column, int row) {
        return new UIRegion(gui, List.of(gui.getSlotFor(column, row)));
    }

    /**
     * Creates a region designating an ordered list of slots.
     *
     * @param gui GUI this region is for.
     * @param slots List of slot numbers this region is for. Does not have to be rectangular.
     * @return A region covering the singular slot.
     */
    public static UIRegion list(SimpleGuiExt gui, Integer... slots) {
        return new UIRegion(gui, List.of(slots));
    }

    /**
     * Creates a region designating an ordered list of slots.
     *
     * @param gui GUI this region is for.
     * @param slots List of slot numbers this region is for. Does not have to be rectangular.
     * @return A region covering the singular slot.
     */
    public static UIRegion list(SimpleGuiExt gui, List<Integer> slots) {
        return new UIRegion(gui, List.copyOf(slots));
    }

    /**
     * Creates a rectangular region of slots.
     *
     * @param gui GUI this region is for.
     * @param startColumnInclusive Starting column for the rectangle, inclusive.
     * @param startRowInclusive Starting row for the rectangle, inclusive.
     * @param endColumnExclusive Ending column for the rectangle, exclusive.
     * @param endRowExclusive Ending row for the rectangle, exclusive.
     * @return A region covering the given rectangle of slots.
     */
    public static UIRectangle rectangle(SimpleGuiExt gui, int startColumnInclusive, int startRowInclusive, int endColumnExclusive, int endRowExclusive) {
        return UIRectangle.create(gui, startColumnInclusive, startRowInclusive, endColumnExclusive, endRowExclusive);
    }

    /**
     * Creates a rectangular region of slots in the player's inventory.
     *
     * @param gui GUI this region is for.
     * @param startColumnInclusive Starting column for the rectangle, inclusive.
     * @param startRowInclusive Starting row for the rectangle, inclusive.
     * @param endColumnExclusive Ending column for the rectangle, exclusive.
     * @param endRowExclusive Ending row for the rectangle, exclusive.
     * @return A region covering the given rectangle of slots in a player's inventory.
     */
    public static UIRectangle playerRectangle(SimpleGuiExt gui, int startColumnInclusive, int startRowInclusive, int endColumnExclusive, int endRowExclusive) {
        return UIRectangle.createPlayer(gui, startColumnInclusive, startRowInclusive, endColumnExclusive, endRowExclusive);
    }

    /**
     * Creates a region covering a row of slots, between two columns.
     * @param gui GUI this region is for.
     * @param row Row being covered.
     * @param startColumnInclusive Starting column for the row, inclusive.
     * @param endColumnExclusive Ending column for the row, exclusive.
     * @return A region covering the given row, between two columns.
     */
    public static UIRectangle row(SimpleGuiExt gui, int row, int startColumnInclusive, int endColumnExclusive) {
        return rectangle(gui, startColumnInclusive, row, endColumnExclusive, row + 1);
    }

    /**
     * Creates a region covering a row of slots in the player's inventory, between two columns.
     * @param gui GUI this region is for.
     * @param row Row being covered.
     * @param startColumnInclusive Starting column for the row, inclusive.
     * @param endColumnExclusive Ending column for the row, exclusive.
     * @return A region covering the given row of slots in the player's inventory, between two columns.
     */
    public static UIRectangle playerRow(SimpleGuiExt gui, int row, int startColumnInclusive, int endColumnExclusive) {
        return playerRectangle(gui, startColumnInclusive, row, endColumnExclusive, row + 1);
    }

    /**
     * Creates a region covering a whole row of slots.
     * @param gui GUI this region is for.
     * @param row Row being covered.
     * @return A region covering the given row.
     */
    public static UIRectangle row(SimpleGuiExt gui, int row) {
        return row(gui, row, 0, gui.getWidth());
    }

    /**
     * Creates a region covering a whole row of slots in the player's inventory.
     * @param gui GUI this region is for.
     * @param row Row being covered.
     * @return A region covering the given row of slots in the player's inventory.
     */
    public static UIRectangle playerRow(SimpleGuiExt gui, int row) {
        return playerRow(gui, row, 0, 9);
    }

    /**
     * Creates a region covering a column of slots, between two rows.
     * @param gui GUI this region is for.
     * @param column Column being covered.
     * @param startRowInclusive Starting row for the column, inclusive.
     * @param endRowExclusive Ending row for the column, exclusive.
     * @return A region covering the given column, between two rows.
     */
    public static UIRectangle column(SimpleGuiExt gui, int column, int startRowInclusive, int endRowExclusive) {
        return rectangle(gui, column, startRowInclusive, column + 1, endRowExclusive);
    }

    /**
     * Creates a region covering a column of slots in the player's inventory, between two rows.
     * @param gui GUI this region is for.
     * @param column Column being covered.
     * @param startRowInclusive Starting row for the column, inclusive.
     * @param endRowExclusive Ending row for the column, exclusive.
     * @return A region covering the given column of slots in the player's inventory, between two rows.
     */
    public static UIRectangle playerColumn(SimpleGuiExt gui, int column, int startRowInclusive, int endRowExclusive) {
        return playerRectangle(gui, column, startRowInclusive, column + 1, endRowExclusive);
    }

    /**
     * Creates a region covering a whole column of slots.
     * @param gui GUI this region is for.
     * @param column Column being covered.
     * @return A region covering the given column.
     */
    public static UIRectangle column(SimpleGuiExt gui, int column) {
        return column(gui, column, 0, gui.getHeight());
    }

    /**
     * Creates a region covering a whole column of slots in the player's inventory.
     * @param gui GUI this region is for.
     * @param column Column being covered.
     * @return A region covering the given column of slots in the player's inventory.
     */
    public static UIRectangle playerColumn(SimpleGuiExt gui, int column) {
        return playerColumn(gui, column, 0, 4);
    }

    /**
     * Clears all slots for this region.
     */
    public void clearSlots() {
        stream().forEach(this.gui::clearSlot);
    }

    /**
     * Fill all slots of this region with the given {@link ItemStack}s.
     * @param stack Stacks to fill slots with.
     */
    public void fillStack(Supplier<ItemStack> stack) {
        stream().forEach(slot -> this.gui.setSlot(slot, stack.get()));
    }

    /**
     * Fill all slots of this region with the given SGUI {@link GuiElementInterface}s.
     * @param element GuiElements to fill slots with.
     */
    public void fillElement(Supplier<GuiElementInterface> element) {
        stream().forEach(slot -> this.gui.setSlot(slot, element.get()));
    }

    /**
     * Fill all slots of this region with the given SGUI {@link GuiElementBuilderInterface}s.
     * @param elementBuilder GuiElementBuilders to fill slots with.
     */
    public void fillElementBuilder(Supplier<GuiElementBuilderInterface<?>> elementBuilder) {
        stream().forEach(slot -> this.gui.setSlot(slot, elementBuilder.get()));
    }

    /**
     * <p>Clear the region, and fill slots with ItemStacks from the given list.</p>
     *
     * <p>If there are less slots than list elements then the list will be truncated; if there are less list elements
     * than slots then empty slots will be set.</p>
     *
     * @see List#subList(int, int)
     * @param stacks ItemStacks to place in this region's slots.
     */
    public void loadStacks(List<ItemStack> stacks) {
        clearSlots();
        Streams.forEachPair(stream(), stacks.stream(), this.gui::setSlot);
    }

    /**
     * <p>Clear the region, and fill slots with SGUI {@link GuiElementInterface}s from the given list.</p>
     *
     * <p>If there are less slots than list elements then the list will be truncated; if there are less list elements
     * than slots then empty slots will be set.</p>
     *
     * @see List#subList(int, int)
     * @param elements GuiElementInterfaces to place in this region's slots.
     */
    public void loadElements(List<? extends GuiElementInterface> elements) {
        clearSlots();
        Streams.forEachPair(stream(), elements.stream(), this.gui::setSlot);
    }

    /**
     * <p>Clear the region, and fill slots with SGUI {@link GuiElementInterface}s from the given list.</p>
     *
     * <p>If there are less slots than list elements then the list will be truncated; if there are less list elements
     * than slots then empty slots will be set.</p>
     *
     * @see List#subList(int, int)
     * @param elementStream GuiElementInterfaces to place in this region's slots.
     */
    public void loadElements(Stream<? extends GuiElementInterface> elementStream) {
        clearSlots();
        Streams.forEachPair(stream(), elementStream, this.gui::setSlot);
    }

    /**
     * <p>Clear the region, and fill slots with SGUI {@link GuiElementBuilderInterface}s from the given list.</p>
     *
     * <p>If there are less slots than list elements then the list will be truncated; if there are less list elements
     * than slots then empty slots will be set.</p>
     *
     * @see List#subList(int, int)
     * @param elementBuilders GuiElementBuilderInterfaces to place in this region's slots.
     */
    public void loadElementBuilders(List<GuiElementBuilderInterface<?>> elementBuilders) {
        clearSlots();
        Streams.forEachPair(stream(), elementBuilders.stream(), this.gui::setSlot);
    }

    /**
     * Get the GUI this region is for.
     * @return This region's GUI.
     */
    public SimpleGuiExt getGui() {
        return this.gui;
    }

    /**
     * Stream all slots covered by this region.
     * @return A stream of all this region's slots.
     */
    public Stream<Integer> stream() {
        return this.slots.stream();
    }

    /**
     * Get all slots for this region.
     *
     * @return An unmodifiable list of all slots covered by this region.
     */
    public List<Integer> slots() {
        return this.slots;
    }

    public int getSlot(int index) {
        if (index < 0) index += this.size();
        return this.slots.get(index);
    }

    /**
     * Gets the number of slots for this region.
     * @return The number of slots covered by this region.
     */
    public int size() {
        return this.slots.size();
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return this.slots.iterator();
    }
}
