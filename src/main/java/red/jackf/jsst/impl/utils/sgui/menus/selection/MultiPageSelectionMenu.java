package red.jackf.jsst.impl.utils.sgui.menus.selection;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.WrappedElement;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.GridPaginator;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MultiPageSelectionMenu<T> extends SelectionMenu<T> {
    private final GridPaginator<T> paginator;
    @Nullable
    private final BiPredicate<T, String> filter;
    private String currentFilterText = "";

    private final List<T> filteredOptions = new ArrayList<>();


    MultiPageSelectionMenu(ServerPlayer player,
                           Component title,
                           List<T> options,
                           Function<T, GuiElementInterface> labelFactory,
                           @Nullable BiPredicate<T, String> filter,
                           Consumer<Optional<T>> callback) {
        super(MenuType.GENERIC_9x6, title, player, options, labelFactory, callback);
        this.filter = filter;

        this.paginator = GridPaginator.<T>builder(this)
                .slots(UIRegion.rectangle(this, 0, 0, 8, 6))
                .fullButtons(this.getSlotFor(8, 0), this.getSlotFor(8, 1), this.getSlotFor(8, 2))
                .elements(this.filter != null ? this.filteredOptions : this.options)
                .drawFunction((index, t) -> WrappedElement.builder(labelFactory.apply(t))
                        .leftClick(Translations.select(), () -> {
                            Sounds.UI.click(player);
                            this.complete(t);
                        }).build())
                .build();

        this.updateFilter();
    }

    private void updateFilter() {
        if (this.filter == null) return;

        Predicate<T> predicate = t -> this.filter.test(t, this.currentFilterText);

        this.filteredOptions.clear();
        this.filteredOptions.addAll(this.options.stream()
                .filter(predicate)
                .toList());
    }

    @Override
    protected void drawStatic() {
        this.setSlot(this.getSize() - 1, CommonElements.cancel(() -> {
            Sounds.UI.close(player);
            this.cancel();
        }));
    }

    @Override
    protected void refresh() {
        this.paginator.draw();

        if (this.filter != null)
            this.setSlot(8, 4, JSSTElementBuilder.from(Items.NAME_TAG).ui()
                    .setName(Component.empty().withStyle(Styles.CLEAN).append("\"")
                            .append(Component.literal(this.currentFilterText).withStyle(Styles.POSITIVE))
                            .append("\""))
                    .leftClick(Translations.search(), () -> {
                        Sounds.UI.click(player);

                        InputMenus.string(player)
                                .initial(this.currentFilterText)
                                .title(Translations.search())
                                .start(opt -> {
                                    opt.ifPresent(s -> {
                                        this.currentFilterText = s;
                                        this.updateFilter();
                                    });

                                    this.open();
                                });
                    })
                    .rightClick(Translations.clear(), () -> {
                        Sounds.UI.reset(player);

                        this.currentFilterText = "";
                        this.updateFilter();
                        this.refresh();
                    }));
    }
}
