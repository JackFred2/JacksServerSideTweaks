package red.jackf.jsst.util.sgui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import red.jackf.jsst.util.sgui.selector.PaginatedSelectorMenu;
import red.jackf.jsst.util.sgui.selector.SinglePageSelectorMenu;

import java.util.Collection;
import java.util.function.Consumer;

public class Menus {
    private static final int PAGINATION_THRESHOLD = 52;

    /**
     * Allows a user to select one of a collection of options. Will resize itself if needed, and will paginate. If paginated,
     * a search bar will also be available. Does not close itself; you'll need to do this in the callback.
     */
    public static <T> void selector(ServerPlayer player, Component title, Collection<T> options, LabelMap<T> labelMap, Consumer<PaginatedSelectorMenu.Selection<T>> onSelect) {
        if (options.size() > PAGINATION_THRESHOLD) {
            new PaginatedSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        } else {
            new SinglePageSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        }
    }
}
