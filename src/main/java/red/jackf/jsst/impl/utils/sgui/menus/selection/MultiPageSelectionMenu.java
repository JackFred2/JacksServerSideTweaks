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

public class MultiPageSelectionMenu<T> extends SelectionMenu<T> {
    MultiPageSelectionMenu(ServerPlayer player, Component title, List<T> options, Function<T, GuiElementInterface> labelFactory, Consumer<Optional<T>> callback) {
        super(MenuType.GENERIC_9x6, title, player, options, labelFactory, callback);
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
}
