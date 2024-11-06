package red.jackf.jsst.impl.utils.sgui.elements.pagination;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.region.UIRectangle;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Handles pagination of UI elements in a list format, with each element taking up a set amount of rows. Allows modification
 * of the core list.
 */
public class ListPaginator<T> {
    private final SimpleGuiExt gui;
    private final List<T> elements;
    private final Builder.Modifiable<T> modifiableSettings;
    private final DrawFunction<T> drawFunction;
    private final UIRectangle slots;
    private final UIRectangle elementSlots;
    private final PageButtons pageButtons;

    private final int pageSize;
    private int maxPage = 0;
    private int page = 0;

    private ListPaginator(SimpleGuiExt gui, List<T> elements, @Nullable ListPaginator.Builder.Modifiable<T> modifiableSettings, DrawFunction<T> drawFunction, UIRectangle slots, PageButtons pageButtons) {
        this.gui = gui;
        this.elements = elements;
        this.modifiableSettings = modifiableSettings;
        this.drawFunction = drawFunction;
        this.slots = slots;
        this.elementSlots = slots.subRows(0, slots.height() - 1); // remove bottom row for use in page buttons and new element button
        this.pageButtons = pageButtons;

        this.pageSize = this.elementSlots.height();
        this.refreshPageCounts();
    }

    private void refreshPageCounts() {
        this.maxPage = Math.max(0, Mth.positiveCeilDiv(this.elements.size(), this.pageSize) - 1);
        this.page = Mth.clamp(this.page, 0, this.maxPage);
    }

    public void draw() {
        this.refreshPageCounts();

        this.slots.clearSlots();

        final int startIndex = this.page * this.pageSize;
        final int endIndex = Math.min(this.elements.size(), (1 + this.page) * this.pageSize);

        List<T> shown = this.elements.subList(startIndex, endIndex);

        int row;
        for (row = 0; row < shown.size(); row++) {
            UIRegion rowSlots = this.elementSlots.rows().get(row);
            T element = shown.get(row);
            final int elementIndex = startIndex + row;
            List<GuiElementInterface> rowElements = this.drawFunction.draw(elementIndex, element);

            int usableRowWidth = this.elementSlots.width();
            if (this.modifiableSettings != null) {
                usableRowWidth--; // delete element button
                if (this.modifiableSettings.allowReordering) {
                    usableRowWidth--; // reorder button
                }
            }

            if (rowElements.size() > usableRowWidth) {
                JSST.LOGGER.warn("Too many elements for row: {} > {}", rowElements.size(), usableRowWidth);
                rowElements = rowElements.subList(0, usableRowWidth);
            }

            rowSlots.loadElements(rowElements);

            if (this.modifiableSettings != null) {
                this.gui.setSlot(rowSlots.getSlot(-1), CommonElements.delete(() -> {
                    Sounds.UI.close(this.gui.getPlayer());
                    this.elements.remove(elementIndex);
                    this.modifiableSettings.changeCallback.run();
                }));

                if (this.modifiableSettings.allowReordering && this.elements.size() > 1) {
                    JSSTElementBuilder builder = JSSTElementBuilder.from(Items.COMMAND_BLOCK).ui();

                    if (elementIndex > 0) {
                        builder.leftClick(Translations.moveUp(), () -> {
                            Sounds.UI.click(this.gui.getPlayer());
                            this.elements.add(elementIndex - 1, this.elements.remove(elementIndex));
                            this.modifiableSettings.changeCallback.run();
                        });
                    }

                    if (elementIndex < this.elements.size() - 1) {
                        builder.rightClick(Translations.moveDown(), () -> {
                            Sounds.UI.click(this.gui.getPlayer());
                            this.elements.add(elementIndex + 1, this.elements.remove(elementIndex));
                            this.modifiableSettings.changeCallback.run();
                        });
                    }

                    this.gui.setSlot(rowSlots.getSlot(-2), builder);
                }
            }
        }

        // new element button
        if (this.modifiableSettings != null && this.elements.size() < this.modifiableSettings.maxElements && this.page == this.maxPage) {
            this.gui.setSlot(this.slots.getSlot(0, row), JSSTElementBuilder.from(Items.NETHER_STAR).ui()
                    .leftClick(Translations.add(), () -> {
                        Sounds.UI.click(this.gui.getPlayer());
                        this.elements.add(this.modifiableSettings.elementSupplier.get());
                        this.modifiableSettings.changeCallback.run();
                    }));
        }

        this.pageButtons.draw(this.gui, this.page, this.maxPage, newPage -> {
            this.page = newPage;
            this.draw();
        });
    }

    public void fillDisabled() {
        this.elementSlots.fillStack(CommonElements::disabled);
        this.pageButtons.forEach(slot -> this.gui.setSlot(slot, CommonElements.disabled()));
    }

    public interface DrawFunction<T> {
        List<GuiElementInterface> draw(int elementIndex, T element);
    }

    public static <T> Builder<T> builder(SimpleGuiExt gui) {
        return new Builder<>(gui);
    }

    public static class Builder<T> {
        private final SimpleGuiExt gui;
        private UIRectangle slots = null;
        private List<T> elements = null;
        private @Nullable Modifiable<T> modifiable = null;
        private DrawFunction<T> drawFunction = null;

        private Builder(SimpleGuiExt gui) {
            this.gui = gui;
        }

        public Builder<T> slots(UIRectangle rectangle) {
            this.slots = rectangle;
            return this;
        }

        public Builder<T> elements(List<T> elements) {
            this.elements = elements;
            return this;
        }

        public Builder<T> modifiable(Supplier<T> elementSupplier, boolean allowReordering, int maxElements, Runnable changeCallback) {
            this.modifiable = new Modifiable<>(elementSupplier, allowReordering, maxElements, changeCallback);
            return this;
        }

        public Builder<T> drawFunction(DrawFunction<T> drawFunction) {
            this.drawFunction = drawFunction;
            return this;
        }

        public ListPaginator<T> build() {
            Objects.requireNonNull(slots);
            Objects.requireNonNull(elements);
            Objects.requireNonNull(drawFunction);

            PageButtons pageButtons;

            // width of a row, take one away for 'new element' button if needed.
            int usableWidth = this.slots.width() - (this.modifiable != null ? 1 : 0);
            if (usableWidth >= 3) { // wide page buttons
                pageButtons = new PageButtons(this.slots.getSlot(-3, -1), this.slots.getSlot(-2, -1), this.slots.getSlot(-1, -1));

            } else {
                throw new NotImplementedException("Small buttons not made yet");
            }

            return new ListPaginator<>(gui, elements, modifiable, drawFunction, slots, pageButtons);
        }

        private record Modifiable<T>(Supplier<T> elementSupplier, boolean allowReordering, int maxElements, Runnable changeCallback) {}
    }
}
