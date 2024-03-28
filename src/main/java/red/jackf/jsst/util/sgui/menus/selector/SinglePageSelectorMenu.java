package red.jackf.jsst.util.sgui.menus.selector;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.util.Collection;
import java.util.function.Consumer;

public final class SinglePageSelectorMenu<T> extends SelectorMenu<T> {
    SinglePageSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Filter<T> filter, LabelMap<T> labelMap, Consumer<Result<T>> callback) {
        super(getSmallestFitting(options.size()).getFirst(), title, player, options, filter, labelMap, callback);

        this.setSlot(getSmallestFitting(this.options.size()).getSecond() - 1, CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.finish(Result.empty());
        }));

        this.redraw();
    }

    private void redraw() {
        var options = getOptions();

        for (int i = 0; i < getSmallestFitting(this.options.size()).getSecond() - 2; i++) {
            if (i < options.size()) {
                T option = options.get(i);
                this.setSlot(i, JSSTElementBuilder.from(labelMap.getLabel(option))
                        .leftClick(Translations.select(), () -> {
                            Sounds.click(player);
                            this.finish(Result.of(option));
                        }));
            } else {
                this.clearSlot(i);
            }
        }

        if (this.filter != null) {
            this.setSlot(getSmallestFitting(this.options.size()).getSecond() - 2, this.filter.filter().buttonBuilder().get()
                    .initial(this.filter.active())
                    .setCallback(filterActive -> {
                        Sounds.click(player);
                        this.filter.setActive(filterActive);
                        this.redraw();
                    }).build());
        }
    }

    private static Pair<MenuType<? extends AbstractContainerMenu>, Integer> getSmallestFitting(int options) {
        if (options <= 3) return Pair.of(MenuType.HOPPER, 5);
        if (options <= 7) return Pair.of(MenuType.GENERIC_9x1, 9);
        if (options <= 16) return Pair.of(MenuType.GENERIC_9x2, 18);
        if (options <= 25) return Pair.of(MenuType.GENERIC_9x3, 27);
        if (options <= 34) return Pair.of(MenuType.GENERIC_9x4, 36);
        if (options <= 43) return Pair.of(MenuType.GENERIC_9x5, 45);
        return Pair.of(MenuType.GENERIC_9x6, 54);
    }
}
