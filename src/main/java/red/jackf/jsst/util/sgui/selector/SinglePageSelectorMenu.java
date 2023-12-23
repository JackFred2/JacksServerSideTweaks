package red.jackf.jsst.util.sgui.selector;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.util.Collection;
import java.util.function.Consumer;

public class SinglePageSelectorMenu<T> extends SelectorMenu<T> {
    public SinglePageSelectorMenu(ServerPlayer player, Component title, Collection<T> options, Consumer<Selection<T>> onSelect, LabelMap<T> labelMap) {
        super(getSmallestFitting(options.size()).getFirst(), title, player, options, onSelect, labelMap);

        for (int i = 0; i < this.options.size(); i++) {
            T option = this.options.get(i);
            this.setSlot(i, GuiElementBuilder.from(labelMap.getLabel(option))
                                             .addLoreLine(Hints.leftClick(Component.translatable("mco.template.button.select")))
                                             .setCallback(() -> this.finish(new Selection<>(true, option))));
        }

        this.setSlot(getSmallestFitting(this.options.size()).getSecond() - 1, CommonLabels.close(this::close));
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
