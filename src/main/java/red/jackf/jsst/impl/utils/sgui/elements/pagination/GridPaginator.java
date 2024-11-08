package red.jackf.jsst.impl.utils.sgui.elements.pagination;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.util.Mth;
import red.jackf.jsst.impl.utils.Arguments;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Handles pagination of UI elements in a grid format, with each element taking up 1 slot, including page buttons.
 */
public class GridPaginator<T> {
    private final SimpleGuiExt gui;
    private final List<T> elements;
    private final DrawFunction<T> drawFunction;
    private final UIRegion valueSlots;
    private final PageButtons pageButtons;

    private final int pageSize;
    private int maxPage = 0;
    private int page = 0;

    private GridPaginator(SimpleGuiExt gui, List<T> elements, DrawFunction<T> drawFunction, UIRegion valueSlots, PageButtons pageButtons) {
        Arguments.isGreaterOrEq(valueSlots.size(), 0, "No value slots");

        this.gui = gui;
        this.elements = elements;
        this.drawFunction = drawFunction;
        this.valueSlots = valueSlots;
        this.pageButtons = pageButtons;

        this.pageSize = this.valueSlots.size();
        this.refreshPageCounts();
    }

    private void refreshPageCounts() {
        this.maxPage = Math.max(0, Mth.positiveCeilDiv(this.elements.size(), this.pageSize) - 1);
        this.page = Mth.clamp(this.page, 0, this.maxPage);
    }

    public void draw() {
        this.refreshPageCounts();

        this.valueSlots.loadElements(IntStream.iterate(this.page * this.pageSize, i -> i < this.elements.size(), i -> i + 1)
                .mapToObj(index -> {
                    T element = this.elements.get(index);
                    return this.drawFunction.draw(index, element);
                }));

        this.pageButtons.draw(this.gui, this.page, this.maxPage, newPage -> {
            this.page = newPage;
            this.draw();
        });
    }

    public void fillDisabled() {
        this.valueSlots.fillElement(CommonElements::disabled);
        this.pageButtons.forEach(slot -> this.gui.setSlot(slot, CommonElements.disabled()));
    }

    public static <T> Builder<T> builder(SimpleGuiExt gui) {
        return new Builder<>(gui);
    }

    public interface DrawFunction<T> {
        GuiElementInterface draw(int elementIndex, T element);
    }

    public static class Builder<T> {
        private final SimpleGuiExt gui;
        private List<T> elements = null;
        private DrawFunction<T> drawFunction = null;
        private UIRegion valueSlots = null;
        private PageButtons pageButtons = null;

        private Builder(SimpleGuiExt gui) {
            this.gui = gui;
        }

        public Builder<T> slots(UIRegion slots) {
            this.valueSlots = slots;
            return this;
        }

        public Builder<T> elements(List<T> elements) {
            this.elements = elements;
            return this;
        }

        public Builder<T> fullButtons(int previousButton, int currentPage, int nextButton) {
            this.pageButtons = new PageButtons(previousButton, currentPage, nextButton);
            return this;
        }

        public Builder<T> drawFunction(DrawFunction<T> drawFunction) {
            this.drawFunction = drawFunction;
            return this;
        }

        public GridPaginator<T> build() {
            Objects.requireNonNull(elements);
            Objects.requireNonNull(drawFunction);
            Objects.requireNonNull(valueSlots);
            Objects.requireNonNull(pageButtons);
            return new GridPaginator<>(gui, elements, drawFunction, valueSlots, pageButtons);
        }
    }
}
