package red.jackf.jsst.impl.utils.sgui.menus.selection;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonLabels;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.WrappedElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SinglePageSelectionMenu<T> extends SelectionMenu<T> {
    SinglePageSelectionMenu(ServerPlayer player, Component title, List<T> options, Function<T, GuiElementInterface> labelFactory, Consumer<Optional<T>> callback) {
        super(getSmallestGuiForSize(options.size()), title, player, options, labelFactory, callback);
    }

    @Override
    protected void drawStatic() {
        this.setSlot(this.getSize() - 1, CommonLabels.cancel(() -> {
            Sounds.UI.close(player);
            this.cancel();
        }));
    }

    @Override
    protected void refresh() {
        for (int i = 0; i < options.size(); i++) {
            T option = this.options.get(i);
            this.setSlot(i, WrappedElement.builder(this.labelFactory.apply(option))
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.complete(option);
                    }));
        }
    }

    private static MenuType<?> getSmallestGuiForSize(int size) {
        // smallest that fits <size - 2> slots: 1 for cancel button and 1 gap
        if (size <= 3) return MenuType.HOPPER;
        if (size <= 7) return MenuType.GENERIC_9x1;
        if (size <= 16) return MenuType.GENERIC_9x2;
        if (size <= 25) return MenuType.GENERIC_9x3;
        if (size <= 34) return MenuType.GENERIC_9x4;
        if (size <= 43) return MenuType.GENERIC_9x5;

        return MenuType.GENERIC_9x6;
    }
}
