package red.jackf.jsst.util.sgui.pagination;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Inputs;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.banners.Banners;

import java.util.function.Consumer;

public sealed interface PageButtonStyle {
    void draw(SlotGuiInterface gui, int page, int maxPage, Consumer<Integer> pageChangeCallback);

    record Compact(int buttonSlot) implements PageButtonStyle {
        @Override
        public void draw(SlotGuiInterface gui, int page, int maxPage, Consumer<Integer> pageChangeCallback) {
            final boolean canGoPreviousPage = page > 0;
            final boolean canGoNextPage = page < maxPage;

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

            var builder = GuiElementBuilder.from(icon).setName(Translations.page(page, maxPage));

            if (canGoNextPage)
                builder.addLoreLine(Hints.leftClick(Component.translatable("jsst.common.next")));
            if (canGoPreviousPage)
                builder.addLoreLine(Hints.rightClick(Component.translatable("jsst.common.previous")));

            if (canGoNextPage || canGoPreviousPage) builder.setCallback(clickType -> {
                if (canGoNextPage && clickType == ClickType.MOUSE_LEFT) {
                    int newPage = Math.min(maxPage, page + 1);
                    Sounds.scroll(gui.getPlayer(), (float) newPage / maxPage);
                    pageChangeCallback.accept(newPage);
                } else if (canGoPreviousPage && clickType == ClickType.MOUSE_RIGHT) {
                    int newPage = Math.max(0, page - 1);
                    Sounds.scroll(gui.getPlayer(), (float) newPage / maxPage);
                    pageChangeCallback.accept(newPage);
                }
            });

            gui.setSlot(this.buttonSlot, builder);
        }
    }

    record Normal(int pageDisplay, int previousButtonSlot, int nextButtonSlot, ArrowDirection arrowDirection) implements PageButtonStyle {
        @Override
        public void draw(SlotGuiInterface gui, int page, int maxPage, Consumer<Integer> pageChangeCallback) {
            final boolean canGoPreviousPage = page > 0;
            final boolean canGoNextPage = page < maxPage;

            if (canGoPreviousPage)
                gui.setSlot(this.previousButtonSlot,
                        GuiElementBuilder.from(arrowDirection == ArrowDirection.VERTICAL ? Banners.Arrows.UP : Banners.Arrows.LEFT)
                                .setName(Hints.leftClick(Translations.previous()))
                                .setCallback(Inputs.leftClick(() -> {
                                    int newPage = Math.max(0, page - 1);
                                    Sounds.scroll(gui.getPlayer(), (float) newPage / maxPage);
                                    pageChangeCallback.accept(newPage);
                                })));
            else
                gui.clearSlot(this.previousButtonSlot);

            gui.setSlot(this.pageDisplay,
                    GuiElementBuilder.from(Banners.Arrows.EMPTY).setName(Translations.page(page, maxPage)));

            if (canGoNextPage)
                gui.setSlot(this.nextButtonSlot,
                        GuiElementBuilder.from(arrowDirection == ArrowDirection.VERTICAL ? Banners.Arrows.DOWN : Banners.Arrows.RIGHT)
                                .setName(Hints.leftClick(Translations.next()))
                                .setCallback(Inputs.leftClick(() -> {
                                    int newPage = Math.min(maxPage, page + 1);
                                    Sounds.scroll(gui.getPlayer(), (float) newPage / maxPage);
                                    pageChangeCallback.accept(newPage);
                                })));
            else
                gui.clearSlot(this.nextButtonSlot);
        }
    }

    enum ArrowDirection {
        VERTICAL,
        HORIZONTAL
    }
}
