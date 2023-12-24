package red.jackf.jsst.util.sgui.selector;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PaginatedSelectorMenu<T> extends SelectorMenu<T> {
    private static final int PER_PAGE = 48;
    private final int maxPage;
    private int page = 0;

    public PaginatedSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Consumer<Selection<T>> onSelect, LabelMap<T> labelMap) {
        super(MenuType.GENERIC_9x6, title, player, options, onSelect, labelMap);
        this.maxPage = (options.size() - 1) / PER_PAGE;

        this.setSlot(53, CommonLabels.close(() -> finish(new Selection<>(false, null))));

        this.refresh();
    }

    private void refresh() {
        List<T> options = this.options.subList(page * PER_PAGE, Math.min((page + 1) * PER_PAGE, this.options.size()));

        // refresh options
        for (int i = 0; i < PER_PAGE; i++) {
            int slot = (i / 8) * 9 + i % 8;
            if (i < options.size()) {
                T option = options.get(i);
                ItemStack label = labelMap.getLabel(option);
                this.setSlot(slot, GuiElementBuilder.from(label)
                                                    .addLoreLine(Hints.leftClick(Component.translatable("mco.template.button.select")))
                                                    .setCallback(() -> this.finish(new Selection<>(true, option))));
            } else {
                this.setSlot(slot, ItemStack.EMPTY);
            }
        }

        // refresh scroll
        if (this.page > 0) {
            this.setSlot(8, GuiElementBuilder.from(new ItemStack(Items.RED_CONCRETE))
                                             .setName(Component.translatable("spectatorMenu.previous_page"))
                                             .addLoreLine(Hints.leftClick())
                                             .setCallback(this::previousPage));
        } else {
            this.setSlot(8, ItemStack.EMPTY);
        }

        this.setSlot(17, CommonLabels.simple(Items.PAPER, Component.translatable("book.pageIndicator", this.page + 1, this.maxPage + 1)));

        if (this.page < maxPage) {
            this.setSlot(26, GuiElementBuilder.from(new ItemStack(Items.LIME_CONCRETE))
                                              .setName(Component.translatable("spectatorMenu.next_page"))
                                              .addLoreLine(Hints.leftClick())
                                              .setCallback(this::nextPage));
        } else {
            this.setSlot(26, ItemStack.EMPTY);
        }
    }

    private void previousPage() {
        this.page = Math.max(0, this.page - 1);
        this.refresh();
    }

    private void nextPage() {
        this.page = Math.min(maxPage, this.page + 1);
        this.refresh();
    }
}
