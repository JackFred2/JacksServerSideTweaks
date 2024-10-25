package red.jackf.jsst.impl.utils.sgui.elements.pagination;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.util.Mth;
import red.jackf.jsst.impl.utils.Arguments;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.UIRegion;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Handles pagination of UI elements, including page buttons.
 */
public class GridPaginator<T> {
    private final SimpleGuiExt gui;
    private final List<T> elements;
    private final Function<T, GuiElementInterface> drawFunction;
    private final UIRegion valueSlots;
    private final PageButtons pageButtons;

    private int maxPage = 0;
    private int pageSize = 0;
    private int page = 0;

    private GridPaginator(SimpleGuiExt gui, List<T> elements, Function<T, GuiElementInterface> drawFunction, UIRegion valueSlots, PageButtons pageButtons) {
        Arguments.isGreaterOrEq(valueSlots.size(), 0, "No value slots");

        this.gui = gui;
        this.elements = elements;
        this.drawFunction = drawFunction;
        this.valueSlots = valueSlots;
        this.pageButtons = pageButtons;

        this.refreshPageCounts();
    }

    private void refreshPageCounts() {
        this.pageSize = this.valueSlots.size();
        this.maxPage = Math.max(0, Mth.positiveCeilDiv(this.elements.size(), this.pageSize) - 1);
        this.page = Mth.clamp(this.page, 0, this.maxPage);
    }

    public void draw() {
        this.refreshPageCounts();

        List<T> shown = this.elements.subList(this.page * this.pageSize, Math.min(this.elements.size(), (1 + this.page) * this.pageSize));

        this.valueSlots.loadElements(shown.stream().map(this.drawFunction).toList());

        this.pageButtons.draw(this.gui, this.page, this.maxPage, newPage -> {
            this.page = newPage;
            this.draw();
        });
    }

    public static <T> Builder<T> builder(SimpleGuiExt gui) {
        return new Builder<>(gui);
    }

    public static class Builder<T> {
        private final SimpleGuiExt gui;
        private List<T> elements = null;
        private Function<T, GuiElementInterface> drawFunction = null;
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

        public Builder<T> drawFunction(Function<T, GuiElementInterface> drawFunction) {
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
