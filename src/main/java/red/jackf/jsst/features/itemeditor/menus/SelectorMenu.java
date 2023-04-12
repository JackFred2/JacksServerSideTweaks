package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Presents a selection of options for a user to pick, along with a cancel option
 */
public class SelectorMenu<T> {
    // a 9x5 page with 1 slot reserved for a cancel button
    private static final int SINGLE_PAGE_MAXIMUM = 44;
    // a 8x5 page, with the 9th column on the right used for page change and cancel button
    private static final int OPTIONS_PER_PAGE = 40;

    private final ServerPlayer player;
    private final Map<T, ItemStack> options;
    private final CancellableCallback<T> callback;

    private int page = 0;
    private final int maxPage;

    protected SelectorMenu(ServerPlayer player, Map<T, ItemStack> options, CancellableCallback<T> callback) {
        this.player = player;
        this.options = options;
        this.callback = callback;
        this.maxPage = options.size() <= SINGLE_PAGE_MAXIMUM ? 0 : (options.size() - 1) / OPTIONS_PER_PAGE;
    }

    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(44, EditorUtils.cancel(callback::cancel));
        if (options.size() > SINGLE_PAGE_MAXIMUM) { // multi page

        } else { // single page
            var index = 0;
            for (var e : options.entrySet()) {
                elements.put(index++, new ItemGuiElement(e.getValue(), () -> {
                    callback.accept(e.getKey());
                }));
            }
        }

        player.openMenu(EditorUtils.make9x5(Component.literal("Select an item"), elements));
    }
}
