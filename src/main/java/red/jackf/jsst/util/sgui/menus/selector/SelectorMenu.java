package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public abstract class SelectorMenu<T> extends SimpleGui {
    private static final int PAGINATION_THRESHOLD = 52;

    protected final List<T> options;
    private final Consumer<Result<T>> onSelect;
    protected final LabelMap<T> labelMap;
    private boolean hasRanCallback = false;

    protected SelectorMenu(MenuType<?> type, Component title, ServerPlayer player, Collection<T> options, Consumer<Result<T>> onSelect, LabelMap<T> labelMap) {
        super(type, player, false);
        this.setTitle(title);
        this.options = List.copyOf(options);
        this.onSelect = onSelect;
        this.labelMap = labelMap;
    }

    /**
     * Allows a user to select one of a collection of options. Will resize itself as needed, and will paginate. If paginated,
     * a search bar will also be available. Does not close itself; you'll need to do this in the callback.
     */
    public static <T> void open(
            ServerPlayer player,
            Component title,
            Collection<T> options,
            LabelMap<T> labelMap,
            Consumer<Result<T>> onSelect) {
        if (options.size() > PAGINATION_THRESHOLD) {
            new PaginatedSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        } else {
            new SinglePageSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        }
    }

    protected void finish(Result<T> selection) {
        if (!hasRanCallback) {
            this.hasRanCallback = true;
            this.onSelect.accept(selection);
        }
    }

    @Override
    public void onClose() {
        finish(Result.empty());
    }
}
