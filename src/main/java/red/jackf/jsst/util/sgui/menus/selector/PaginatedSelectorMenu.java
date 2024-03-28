package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.pagination.GridPaginator;
import red.jackf.jsst.util.sgui.pagination.PageButtonStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class PaginatedSelectorMenu<T> extends SelectorMenu<T> {
    private final List<T> filteredOptions = new ArrayList<>();
    private final GridPaginator<T> paginator = GridPaginator.<T>builder(this)
            .list(() -> filteredOptions)
            .drawFunc(this::getLabelFor)
            .slots(GridTranslator.between(0, 8, 0, 6))
            .normalButtons(Util.slot(8, 1), Util.slot(8, 0), Util.slot(8, 2), PageButtonStyle.ArrowDirection.VERTICAL)
            .build();
    private String nameFilter = "";

    PaginatedSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Filter<T> filter, LabelMap<T> labelMap, Consumer<Result<T>> callback) {
        super(MenuType.GENERIC_9x6, title, player, options, filter, labelMap, callback);
        this.filteredOptions.addAll(getOptions());

        this.setSlot(Util.slot(8, 5), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.finish(Result.empty());
        }));
    }

    private GuiElementInterface getLabelFor(T t) {
        ItemStack label = labelMap.getLabel(t);
        return JSSTElementBuilder.from(label)
                .leftClick(Translations.select(), () -> {
                    Sounds.click(player);
                    this.finish(Result.of(t));
                }).build();
    }

    private void calculateFiltered() {
        this.filteredOptions.clear();
        this.filteredOptions.addAll(getOptions().stream()
                .filter(opt -> labelMap.getLabel(opt).getDisplayName().getString().toLowerCase().contains(this.nameFilter))
                .toList());
        this.redraw();
    }

    @Override
    public void onOpen() {
        this.calculateFiltered();
    }

    private void redraw() {
        this.paginator.draw();

        // search
        this.setSlot(Util.slot(8, 4), JSSTElementBuilder.from(Items.NAME_TAG)
                .setName(Component.translatable("jsst.common.searchFilter", Component.literal(this.nameFilter)
                        .setStyle(Styles.VARIABLE)))
                .leftClick(Translations.change(), () -> {
                    Sounds.click(player);
                    Menus.stringBuilder(player)
                            .title(Translations.search())
                            .initial(this.nameFilter)
                            .createAndShow(result -> {
                                if (result.hasResult())
                                    this.nameFilter = result.result();
                                this.open();
                            });
                }).rightClick(Translations.clear(), () -> {
                    Sounds.clear(player);
                    this.nameFilter = "";
                    this.calculateFiltered();
                }));


        if (this.filter != null) {
            this.setSlot(Util.slot(8, 3), this.filter.filter().buttonBuilder().get()
                    .initial(this.filter.active())
                    .setCallback(filterActive -> {
                        Sounds.click(player);
                        this.filter.setActive(filterActive);
                        this.redraw();
                    }).build());
        }
    }
}
