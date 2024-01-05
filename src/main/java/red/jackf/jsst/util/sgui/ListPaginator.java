package red.jackf.jsst.util.sgui;

import blue.endless.jankson.annotation.Nullable;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.JSST;
import red.jackf.jsst.util.sgui.banners.Banners;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Automatically handles most of a page of items for you, with a vertical list of rows and automatic page button management
 *
 * @param <T>
 */
public class ListPaginator<T> {

    private final SlotGuiInterface gui;
    private final int colFrom;
    private final int colTo;
    private final int rowFrom;
    private final int rowTo;
    private final Supplier<T> newElementSupplier;
    private final int height;
    private final int width;
    private final boolean withReordering;
    private final Runnable onUpdate;
    private final Supplier<List<T>> listSupplier;
    private final int maxItems;
    private final RowDrawCallback<T> rowDraw;
    private final boolean modifiable;

    // -1: not yet calculated
    private int page = 0;
    private int maxPage = 0;

    private ListPaginator(
            SlotGuiInterface gui,
            int colFrom,
            int colTo,
            int rowFrom,
            int rowTo,
            Supplier<List<T>> listSupplier,
            int maxItems,
            RowDrawCallback<T> rowDraw,
            @Nullable Supplier<T> newElementSupplier,
            boolean withReordering,
            Runnable onUpdate) {
        this.gui = gui;
        this.colFrom = colFrom;
        this.colTo = colTo;
        this.rowFrom = rowFrom;
        this.rowTo = rowTo;
        this.listSupplier = listSupplier;
        this.maxItems = maxItems;
        this.rowDraw = rowDraw;
        this.newElementSupplier = newElementSupplier;
        this.withReordering = withReordering;
        this.onUpdate = onUpdate;

        this.width = colTo - colFrom;
        this.height = rowTo - rowFrom;
        this.modifiable = newElementSupplier != null;
    }

    public static <T> Builder<T> builder(SlotGuiInterface gui) {
        return new Builder<>(gui);
    }

    private void onUpdate() {
        this.onUpdate.run();
        this.draw();
    }

    public void draw() {
        List<T> elements = this.listSupplier.get();
        int perPage = this.height - 1;
        this.maxPage = Mth.clamp((elements.size() - 1) / perPage, 0, Mth.positiveCeilDiv(this.maxItems, perPage) - 1);
        this.page = Math.min(this.page, this.maxPage);

        Util.clear(this.gui, this.colFrom, this.colTo, this.rowFrom, this.rowTo);

        List<T> sublist = elements.subList(this.page * perPage, Math.min(elements.size(), (this.page + 1) * perPage));

        int maxCustomIcons = this.width;
        if (this.modifiable) maxCustomIcons -= this.withReordering ? 2 : 1;

        int index;
        for (index = 0; index < perPage && index < sublist.size(); index++) {
            final int fullIndex = this.page * perPage + index;
            T element = sublist.get(index);
            int row = this.rowFrom + index;

            List<GuiElementInterface> rowIcons = this.rowDraw.draw(fullIndex, element);
            if (rowIcons.size() > maxCustomIcons) {
                JSST.LOGGER.warn("Too wide row to render for " + gui.getClass().getSimpleName());
                rowIcons = rowIcons.subList(0, maxCustomIcons);
            }

            // icons
            for (int icon = 0; icon < rowIcons.size(); icon++) {
                int column = this.colFrom + icon;
                this.gui.setSlot(Util.slot(column, row), rowIcons.get(icon));
            }

            if (this.modifiable) {
                // delete row
                this.gui.setSlot(Util.slot(this.colTo - 1, row),
                                 GuiElementBuilder.from(Items.BARRIER.getDefaultInstance())
                                                  .setName(Translations.delete().withStyle(Styles.INPUT_HINT))
                                                  .addLoreLine(Hints.leftClick())
                                                  .setCallback(Inputs.leftClick(() -> {
                                                      Sounds.click(this.gui.getPlayer());
                                                      this.listSupplier.get().remove(fullIndex);
                                                      this.onUpdate();
                                                  })));

                // reorder if applicable
                if (this.withReordering) {
                    final boolean canMoveDown = fullIndex < elements.size() - 1;
                    final boolean canMoveUp = fullIndex > 0;

                    ItemStack icon;
                    if (canMoveDown) {
                        if (canMoveUp) {
                            icon = Banners.Arrows.VERTICAL;
                        } else {
                            icon = Banners.Arrows.DOWN;
                        }
                    } else {
                        if (canMoveUp) {
                            icon = Banners.Arrows.UP;
                        } else {
                            continue;
                        }
                    }

                    var reorder = GuiElementBuilder.from(icon).setName(Component.translatable("jsst.common.reorder"));

                    if (canMoveDown)
                        reorder.addLoreLine(Hints.leftClick(Component.translatable("jsst.common.reorder.down")));
                    if (canMoveUp)
                        reorder.addLoreLine(Hints.rightClick(Component.translatable("jsst.common.reorder.up")));

                    reorder.setCallback(clickType -> {
                        if (canMoveDown && clickType == ClickType.MOUSE_LEFT) {
                            Sounds.click(this.gui.getPlayer());
                            Util.Lists.swap(this.listSupplier.get(), fullIndex, fullIndex + 1);
                            this.onUpdate();
                        } else if (canMoveUp && clickType == ClickType.MOUSE_RIGHT) {
                            Sounds.click(this.gui.getPlayer());
                            Util.Lists.swap(this.listSupplier.get(), fullIndex, fullIndex - 1);
                            this.onUpdate();
                        }
                    });

                    this.gui.setSlot(Util.slot(this.colTo - 2, row), reorder);
                }
            }
        }

        // new element
        if (this.modifiable && elements.size() < this.maxItems && this.page == this.maxPage) {
            this.gui.setSlot(Util.slot(this.colFrom, this.rowFrom + index),
                             GuiElementBuilder.from(Items.NETHER_STAR.getDefaultInstance())
                                              .setName(Translations.add().withStyle(Styles.INPUT_HINT))
                                              .addLoreLine(Hints.leftClick())
                                              .setCallback(Inputs.leftClick(() -> {
                                                  Sounds.click(this.gui.getPlayer());
                                                  this.listSupplier.get().add(this.newElementSupplier.get());
                                                  this.onUpdate();
                                              })));
        }

        // page buttons and indicator
        // if width < 4 (new + 3 for page buttons) use a compact page indicator instead
        final boolean useSmall = this.width < (this.modifiable ? 4 : 3);
        final int lastRow = this.rowTo - 1;

        final boolean canGoPreviousPage = this.page > 0;
        final boolean canGoNextPage = this.page < this.maxPage;

        if (useSmall) {
            ItemStack icon;
            if (canGoPreviousPage) {
                if (canGoNextPage) {
                    icon = Banners.Arrows.HORIZONTAL;
                } else {
                    icon = Banners.Arrows.LEFT;
                }
            } else {
                if (canGoNextPage) {
                    icon = Banners.Arrows.RIGHT;
                } else {
                    icon = Banners.Arrows.EMPTY;
                }
            }

            var builder = GuiElementBuilder.from(icon).setName(Translations.page(this.page, this.maxPage));

            if (canGoPreviousPage) builder.addLoreLine(Hints.rightClick(Component.translatable("jsst.common.previous")));
            if (canGoNextPage) builder.addLoreLine(Hints.leftClick(Component.translatable("jsst.common.next")));

            if (canGoNextPage || canGoPreviousPage) builder.setCallback(clickType -> {
                if (canGoNextPage && clickType == ClickType.MOUSE_LEFT) {
                    this.page = Math.min(this.maxPage, this.page + 1);
                    Sounds.scroll(this.gui.getPlayer(), (float) this.page / this.maxPage);
                    this.draw();
                } else if (canGoPreviousPage && clickType == ClickType.MOUSE_RIGHT) {
                    this.page = Math.max(0, this.page - 1);
                    Sounds.scroll(this.gui.getPlayer(), (float) this.page / this.maxPage);
                    this.draw();
                }
            });

            this.gui.setSlot(Util.slot(this.colTo - 1, lastRow), builder);
        } else {
            if (canGoPreviousPage) this.gui.setSlot(Util.slot(this.colTo - 3, lastRow),
                                                    GuiElementBuilder.from(Banners.Arrows.LEFT)
                                                                     .setName(Translations.previous().withStyle(Styles.INPUT_HINT))
                                                                     .addLoreLine(Hints.leftClick())
                                                                     .setCallback(Inputs.leftClick(() -> {
                                                                         this.page = Math.max(0, this.page - 1);
                                                                         Sounds.scroll(this.gui.getPlayer(), (float) this.page / this.maxPage);
                                                                         this.draw();
                                                                     })));

            this.gui.setSlot(Util.slot(this.colTo - 2, lastRow),
                             GuiElementBuilder.from(Banners.Arrows.EMPTY).setName(Translations.page(this.page, this.maxPage)));

            if (canGoNextPage) this.gui.setSlot(Util.slot(this.colTo - 1, lastRow),
                                                GuiElementBuilder.from(Banners.Arrows.RIGHT)
                                                                 .setName(Translations.next().withStyle(Styles.INPUT_HINT))
                                                                 .addLoreLine(Hints.leftClick())
                                                                 .setCallback(Inputs.leftClick(() -> {
                                                                     this.page = Math.min(this.maxPage, this.page + 1);
                                                                     Sounds.scroll(this.gui.getPlayer(), (float) this.page / this.maxPage);
                                                                     this.draw();
                                                                 })));
        }
    }

    public interface RowDrawCallback<T> {
        List<GuiElementInterface> draw(int index, T element);
    }

    public static class Builder<T> {
        private final SlotGuiInterface gui;
        private int colFrom = -1;
        private int colTo = -1;
        private int rowFrom = -1;
        private int rowTo = -1;
        private int maxItems = Integer.MAX_VALUE;
        private Supplier<List<T>> listSupplier = null;
        private RowDrawCallback<T> rowDraw = null;
        private Runnable onUpdate = () -> {};
        @Nullable
        private Supplier<T> newElementSupplier = null;
        private boolean withReordering = false;

        private Builder(SlotGuiInterface gui) {
            this.gui = gui;
        }

        public Builder<T> at(int colFrom, int colTo, int rowFrom, int rowTo) {
            if (colFrom < 0 || colFrom > gui.getWidth())
                throw new IllegalArgumentException("Out of bounds colFrom: " + colFrom);
            if (colTo < 0 || colTo > gui.getWidth())
                throw new IllegalArgumentException("Out of bounds colTo: " + colTo);
            if (rowFrom < 0 || rowFrom > gui.getHeight())
                throw new IllegalArgumentException("Out of bounds rowFrom: " + rowFrom);
            if (rowTo < 0 || rowTo > gui.getHeight())
                throw new IllegalArgumentException("Out of bounds colFrom: " + rowTo);
            if (colTo - colFrom < 1) throw new IllegalArgumentException("Too small width: " + (colTo - colFrom));
            if (rowTo - rowFrom < 2) throw new IllegalArgumentException("Too small height: " + (rowTo - rowFrom));
            this.colFrom = colFrom;
            this.colTo = colTo;
            this.rowFrom = rowFrom;
            this.rowTo = rowTo;
            return this;
        }

        public Builder<T> list(List<T> list) {
            this.listSupplier = () -> list;
            return this;
        }

        public Builder<T> list(Supplier<List<T>> list) {
            this.listSupplier = list;
            return this;
        }

        public Builder<T> max(int max) {
            this.maxItems = max;
            return this;
        }

        public Builder<T> rowDraw(RowDrawCallback<T> rowDraw) {
            this.rowDraw = rowDraw;
            return this;
        }

        public Builder<T> modifiable(Supplier<T> newElementSupplier, boolean withReordering) {
            this.newElementSupplier = newElementSupplier;
            this.withReordering = withReordering;
            return this;
        }

        public Builder<T> onUpdate(Runnable onUpdate) {
            this.onUpdate = onUpdate;
            return this;
        }

        public ListPaginator<T> build() {
            if (colFrom == -1) throw new IllegalArgumentException("No dimensions set");
            if (this.newElementSupplier != null) {
                int width = colTo - colFrom;
                if (width < (withReordering ? 3 : 2))
                    throw new IllegalArgumentException("Too small width (modifiable requires 2 or 3 with reordering): " + width);
            }
            Objects.requireNonNull(listSupplier);
            Objects.requireNonNull(rowDraw);
            return new ListPaginator<>(gui,
                                       colFrom,
                                       colTo,
                                       rowFrom,
                                       rowTo,
                                       listSupplier,
                                       maxItems,
                                       rowDraw,
                                       newElementSupplier,
                                       withReordering,
                                       onUpdate);
        }
    }
}
