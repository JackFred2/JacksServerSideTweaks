package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.banners.Banners;
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

    public PaginatedSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Consumer<Result<T>> onSelect, LabelMap<T> labelMap) {
        super(MenuType.GENERIC_9x6, title, player, options, onSelect, labelMap);
        this.filteredOptions = new ArrayList<>(this.options.size());

        this.setSlot(Util.slot(8, 5), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.finish(Result.empty());
        }));
    }

    private void calculateFiltered() {
        this.filteredOptions.clear();
        this.filteredOptions.addAll(this.options.stream()
                                                .filter(opt -> labelMap.getLabel(opt).getDisplayName().getString().toLowerCase().contains(this.filter))
                                                .toList());
        this.maxPage = Math.max(0, (this.filteredOptions.size() - 1) / PER_PAGE);
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
            int col = i % 8;
            int row = i / 8;
            if (i < options.size()) {
                T option = options.get(i);
                ItemStack label = labelMap.getLabel(option);
                this.setSlot(Util.slot(col, row), GuiElementBuilder.from(label)
                                                    .addLoreLine(Hints.leftClick(Translations.select()))
                                                    .setCallback(Inputs.leftClick(() -> {
                                                        Sounds.click(player);
                                                        this.finish(Result.of(option));
                                                    })));
            } else {
                this.setSlot(Util.slot(col, row), ItemStack.EMPTY);
            }
        }

        // refresh scroll
        if (this.page > 0) {
            this.setSlot(Util.slot(8, 0), GuiElementBuilder.from(Banners.Arrows.UP)
                                             .setName(Component.translatable("spectatorMenu.previous_page").withStyle(Styles.INPUT_HINT))
                                             .addLoreLine(Hints.leftClick())
                                             .setCallback(Inputs.leftClick(this::previousPage)));
        } else {
            this.setSlot(Util.slot(8, 0), ItemStack.EMPTY);
        }

        this.setSlot(Util.slot(8, 1), CommonLabels.simple(Banners.Arrows.EMPTY, Translations.page(this.page, this.maxPage)));

        if (this.page < maxPage) {
            this.setSlot(Util.slot(8, 2), GuiElementBuilder.from(Banners.Arrows.DOWN)
                                              .setName(Component.translatable("spectatorMenu.next_page").withStyle(Styles.INPUT_HINT))
                                              .addLoreLine(Hints.leftClick())
                                              .setCallback(Inputs.leftClick(this::nextPage)));
        } else {
            this.setSlot(Util.slot(8, 2), ItemStack.EMPTY);
        }

        // search
        this.setSlot(Util.slot(8, 4), GuiElementBuilder.from(new ItemStack(Items.NAME_TAG))
                .setName(Component.translatable("jsst.common.searchFilter", Component.literal(this.filter).setStyle(Styles.VARIABLE)))
                .addLoreLine(Hints.leftClick(Translations.change()))
                .addLoreLine(Hints.rightClick(Translations.clear()))
                .setCallback(this::openFilter));
    }

    private void openFilter(ClickType type) {
        if (type == ClickType.MOUSE_LEFT) {
            Sounds.click(player);
            Menus.stringBuilder(player)
                 .title(Translations.search())
                 .initial(this.filter)
                 .createAndShow(result -> {
                     if (result.hasResult())
                         this.filter = result.result();
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
