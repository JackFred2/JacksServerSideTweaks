package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.Result;
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

public class PaginatedSelectorMenu<T> extends SelectorMenu<T> {
    protected final List<T> filteredOptions = new ArrayList<>();
    private final GridPaginator<T> paginator = GridPaginator.<T>builder(this)
            .list(() -> filteredOptions)
            .drawFunc(this::getLabelFor)
            .slots(GridTranslator.between(0, 8, 0, 6))
            .normalButtons(Util.slot(8, 1), Util.slot(8, 0), Util.slot(8, 2), PageButtonStyle.ArrowDirection.VERTICAL)
            .build();
    private String filter = "";

    public PaginatedSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Consumer<Result<T>> onSelect, LabelMap<T> labelMap) {
        super(MenuType.GENERIC_9x6, title, player, options, onSelect, labelMap);
        this.filteredOptions.addAll(this.options);

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
        this.filteredOptions.addAll(this.options.stream()
                .filter(opt -> labelMap.getLabel(opt).getDisplayName().getString().toLowerCase().contains(this.filter))
                .toList());
        this.refresh();
    }

    @Override
    public void onOpen() {
        this.calculateFiltered();
    }

    private void refresh() {
        this.paginator.draw();

        // search
        this.setSlot(Util.slot(8, 4), JSSTElementBuilder.from(Items.NAME_TAG)
                .setName(Component.translatable("jsst.common.searchFilter", Component.literal(this.filter)
                        .setStyle(Styles.VARIABLE)))
                .leftClick(Translations.change(), () -> {
                    Sounds.click(player);
                    Menus.stringBuilder(player)
                            .title(Translations.search())
                            .initial(this.filter)
                            .createAndShow(result -> {
                                if (result.hasResult())
                                    this.filter = result.result();
                                this.open();
                            });
                }).rightClick(Translations.clear(), () -> {
                    Sounds.clear(player);
                    this.filter = "";
                    this.calculateFiltered();
                }));
    }
}
