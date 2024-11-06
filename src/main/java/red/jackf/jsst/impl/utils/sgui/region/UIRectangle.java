package red.jackf.jsst.impl.utils.sgui.region;

import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.impl.utils.Arguments;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Designates a set of slots that are specifically a rectangular region in a GUI. Use the methods in {@link UIRegion} to create.
 */
public class UIRectangle extends UIRegion {
    private static final Set<MenuType<?>> RECTANGULAR_MENUS = Set.of(
            MenuType.GENERIC_9x1,
            MenuType.GENERIC_9x2,
            MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x4,
            MenuType.GENERIC_9x5,
            MenuType.GENERIC_9x6,
            MenuType.GENERIC_3x3,
            MenuType.HOPPER,
            MenuType.SHULKER_BOX
    );
    private final List<UIRegion> rows;

    private UIRectangle(SimpleGuiExt gui, List<UIRegion> rows) {
        super(gui, rows.stream().flatMap(UIRegion::stream).toList());
        this.rows = List.copyOf(rows);
    }

    public UIRectangle subRows(int fromRowInclusive, int toRowExclusive) {
        return new UIRectangle(this.getGui(), this.rows.subList(fromRowInclusive, toRowExclusive));
    }

    public int width() {
        return rows.getFirst().size();
    }

    public int height() {
        return rows.size();
    }

    public int getSlot(int column, int row) {
        if (column < 0) column += this.width();
        if (row < 0) row += this.height();

        Arguments.inRange(column, 0, this.width(), "column out of range: %d (%d)", column);
        Arguments.inRange(row, 0, this.height(), "column out of range: %d (%d)", row);

        return this.slots().get(this.width() * row + column);
    }

    public List<UIRegion> rows() {
        return rows;
    }

    protected static UIRectangle create(SimpleGuiExt gui, int startColumnInclusive, int startRowInclusive, int endColumnExclusive, int endRowExclusive) {
        Arguments.inRange(startColumnInclusive, 0, gui.getWidth(), "startColumn out of range: %d");
        Arguments.inRange(startRowInclusive, 0, gui.getHeight(), "startRow out of range: %d");
        Arguments.inRange(endColumnExclusive, 0, gui.getWidth(), "endColumn out of range: %d");
        Arguments.inRange(endRowExclusive, 0, gui.getHeight(), "startColumn out of range: %d");
        Arguments.isLessOrEq(startColumnInclusive, endColumnExclusive, "startColumn > endColumn: %d > %d");
        Arguments.isLessOrEq(startRowInclusive, endRowExclusive, "startRow > endRow: %d > %d");
        if (!RECTANGULAR_MENUS.contains(gui.getType())) {
            throw new IllegalArgumentException("Non rectangular gui for UIRectangle");
        }

        List<UIRegion> rows = new ArrayList<>();

        for (int row = startRowInclusive; row < endRowExclusive; row++) {
            List<Integer> rowSlots = new ArrayList<>();

            for (int col = startColumnInclusive; col < endColumnExclusive; col++) {
                rowSlots.add(gui.getSlotFor(col, row));
            }

            rows.add(UIRegion.list(gui, rowSlots));
        }

        return new UIRectangle(gui, rows);
    }

    protected static UIRectangle createPlayer(SimpleGuiExt gui, int startColumnInclusive, int startRowInclusive, int endColumnExclusive, int endRowExclusive) {
        Arguments.inRange(startColumnInclusive, 0, 9, "startColumn out of range: %d");
        Arguments.inRange(startRowInclusive, 0, 4, "startRow out of range: %d");
        Arguments.inRange(endColumnExclusive, 0, 9, "endColumn out of range: %d");
        Arguments.inRange(endRowExclusive, 0, 4, "startColumn out of range: %d");
        Arguments.isLessOrEq(startColumnInclusive, endColumnExclusive, "startColumn > endColumn: %d > %d");
        Arguments.isLessOrEq(startRowInclusive, endRowExclusive, "startRow > endRow: %d > %d");

        List<UIRegion> rows = new ArrayList<>();

        for (int row = startRowInclusive; row < endRowExclusive; row++) {
            List<Integer> rowSlots = new ArrayList<>();

            for (int col = startColumnInclusive; col < endColumnExclusive; col++) {
                rowSlots.add(gui.getVirtualSize() + row * 9 + col);
            }

            rows.add(UIRegion.list(gui, rowSlots));
        }

        return new UIRectangle(gui, rows);
    }
}
