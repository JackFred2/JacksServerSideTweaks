package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PaginatedSelectorMenu<T> extends SelectorMenu<T> {
    private static final int PER_PAGE = 48;
    protected final List<T> filteredOptions;
    private String filter = "";
    private int maxPage;
    private int page = 0;

    public PaginatedSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Consumer<Selection<T>> onSelect, LabelMap<T> labelMap) {
        super(MenuType.GENERIC_9x6, title, player, options, onSelect, labelMap);
        this.filteredOptions = new ArrayList<>(this.options.size());

        this.setSlot(53, CommonLabels.close(() -> {
            Sounds.close(player);
            this.finish(new Selection<>(false, null));
        }));
    }

    private void calculateFiltered() {
        this.filteredOptions.clear();
        this.filteredOptions.addAll(this.options.stream()
                                                .filter(opt -> labelMap.getLabel(opt).getDisplayName().getString().toLowerCase().contains(this.filter))
                                                .toList());
        this.maxPage = (this.filteredOptions.size() - 1) / PER_PAGE;
        this.page = Math.min(maxPage, page);
        this.refresh();
    }

    @Override
    public void onOpen() {
        this.calculateFiltered();
    }

    private void refresh() {
        List<T> options = this.filteredOptions.subList(page * PER_PAGE, Math.min((page + 1) * PER_PAGE, this.filteredOptions.size()));

        // refresh options
        for (int i = 0; i < PER_PAGE; i++) {
            int slot = (i / 8) * 9 + i % 8;
            if (i < options.size()) {
                T option = options.get(i);
                ItemStack label = labelMap.getLabel(option);
                this.setSlot(slot, GuiElementBuilder.from(label)
                                                    .addLoreLine(Hints.leftClick(Component.translatable("mco.template.button.select")))
                                                    .setCallback(Inputs.leftClick(() -> {
                                                        Sounds.click(player);
                                                        this.finish(new Selection<>(true, option));
                                                    })));
            } else {
                this.setSlot(slot, ItemStack.EMPTY);
            }
        }

        // refresh scroll
        if (this.page > 0) {
            this.setSlot(8, GuiElementBuilder.from(new ItemStack(Items.RED_CONCRETE))
                                             .setName(Component.translatable("spectatorMenu.previous_page"))
                                             .addLoreLine(Hints.leftClick())
                                             .setCallback(Inputs.leftClick(this::previousPage)));
        } else {
            this.setSlot(8, ItemStack.EMPTY);
        }

        this.setSlot(17, CommonLabels.simple(Items.PAPER, Component.translatable("book.pageIndicator", this.page + 1, this.maxPage + 1)));

        if (this.page < maxPage) {
            this.setSlot(26, GuiElementBuilder.from(new ItemStack(Items.LIME_CONCRETE))
                                              .setName(Component.translatable("spectatorMenu.next_page"))
                                              .addLoreLine(Hints.leftClick())
                                              .setCallback(Inputs.leftClick(this::nextPage)));
        } else {
            this.setSlot(26, ItemStack.EMPTY);
        }

        // search
        this.setSlot(44, GuiElementBuilder.from(new ItemStack(Items.NAME_TAG))
                .setName(Component.translatable("jsst.common.searchFilter", Component.literal(this.filter).setStyle(Styles.VARIABLE)))
                .addLoreLine(Hints.leftClick(Translations.change()))
                .addLoreLine(Hints.rightClick(Translations.clear()))
                .setCallback(this::openFilter));
    }

    private void openFilter(ClickType type) {
        if (type == ClickType.MOUSE_LEFT) {
            Sounds.click(player);
            Menus.string(player, Translations.search(), this.filter, null, opt -> {
                opt.ifPresent(s -> this.filter = s);
                this.open();
            });
        } else if (type == ClickType.MOUSE_RIGHT) {
            Sounds.clear(player);
            this.filter = "";
            this.calculateFiltered();
        }
    }

    private void previousPage() {
        this.page = Math.max(0, this.page - 1);
        Sounds.scroll(player, (float) this.page / this.maxPage);
        this.refresh();
    }

    private void nextPage() {
        this.page = Math.min(maxPage, this.page + 1);
        Sounds.scroll(player, (float) this.page / this.maxPage);
        this.refresh();
    }
}
