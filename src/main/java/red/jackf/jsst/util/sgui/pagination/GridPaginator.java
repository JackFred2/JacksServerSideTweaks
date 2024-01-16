package red.jackf.jsst.util.sgui.pagination;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.GridTranslator;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class GridPaginator<T> {

    private final SlotGuiInterface gui;
    private final GridTranslator slots;
    private final Supplier<List<T>> listSupplier;
    private final Function<T, GuiElementInterface> drawFunc;
    private final PageButtonStyle pageButtons;

    private int page = 0;

    private GridPaginator(SlotGuiInterface gui,
                          GridTranslator slots,
                          Supplier<List<T>> listSupplier,
                          Function<T, GuiElementInterface> drawFunc,
                          PageButtonStyle pageButtons) {

        this.gui = gui;
        this.slots = slots;
        this.listSupplier = listSupplier;
        this.drawFunc = drawFunc;
        this.pageButtons = pageButtons;
    }

    public static <T> Builder<T> builder(SlotGuiInterface gui) {
        return new Builder<>(gui);
    }

    public void draw() {
        this.slots.fill(this.gui, ItemStack.EMPTY);

        List<T> elements = this.listSupplier.get();
        if (elements.isEmpty()) return;

        int perPage = this.slots.size();
        int maxPage = Mth.clamp((elements.size() - 1) / perPage, 0, Mth.positiveCeilDiv(elements.size() - 1, perPage));
        this.page = Math.min(this.page, maxPage);

        List<T> sublist = elements.subList(this.page * perPage, Math.min(elements.size(), (this.page + 1) * perPage));

        for (GridTranslator.SlotItemPair<T> pair : this.slots.iterate(sublist)) {
            this.gui.setSlot(pair.slot(), this.drawFunc.apply(pair.item()));
        }

        this.pageButtons.draw(this.gui, this.page, maxPage, newPage -> {
            this.page = newPage;
            this.draw();
        });
    }

    public static class Builder<T> {
        private final SlotGuiInterface gui;
        private GridTranslator slots;
        private Supplier<List<T>> listSupplier = null;
        private Function<T, GuiElementInterface> drawFunc = null;
        private PageButtonStyle pageButtons = null;

        private Builder(SlotGuiInterface gui) {
            this.gui = gui;
        }

        public Builder<T> slots(GridTranslator slots) {
            Objects.requireNonNull(slots);
            this.slots = slots;
            return this;
        }

        public Builder<T> list(List<T> list) {
            return list(() -> list);
        }

        public Builder<T> list(Supplier<List<T>> list) {
            this.listSupplier = list;
            return this;
        }

        public Builder<T> drawFunc(Function<T, GuiElementInterface> drawFunc) {
            this.drawFunc = drawFunc;
            return this;
        }

        // single-item control
        public Builder<T> compactButtons(int pageSlot) {
            this.pageButtons = new PageButtonStyle.Compact(pageSlot);
            return this;
        }

        // multi-item controls
        public Builder<T> normalButtons(int pageSlot, int previousSlot, int nextSlot, PageButtonStyle.ArrowDirection arrowDirection) {
            this.pageButtons = new PageButtonStyle.Normal(pageSlot, previousSlot, nextSlot, arrowDirection);
            return this;
        }

        public GridPaginator<T> build() {
            Objects.requireNonNull(listSupplier);
            Objects.requireNonNull(drawFunc);
            Objects.requireNonNull(slots);
            Objects.requireNonNull(pageButtons);
            return new GridPaginator<>(gui, slots, listSupplier, drawFunc, pageButtons);
        }
    }
}
