package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.HashMap;
import java.util.Map;

/**
 * Presents a selection of options for a user to pick, along with a cancel option.
 * It is highly recommended to use a {@link java.util.LinkedHashMap} or similar for order consistency.
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
        var entries = options.entrySet();
        if (options.size() > SINGLE_PAGE_MAXIMUM) { // multi page
            this.page = Mth.clamp(page, 0, maxPage);
            var toAdd = entries.stream().skip((long) page * OPTIONS_PER_PAGE).limit(OPTIONS_PER_PAGE).toList();
            var index = 0;
            for (var entry : toAdd) {
                var slot = 9 * (index / 8) + index % 8;
                elements.put(slot, new ItemGuiElement(entry.getValue(), () -> callback.accept(entry.getKey())));
                index++;
            }

            if (page > 0)
                elements.put(8, new ItemGuiElement(Labels.create(Items.RED_CONCRETE).withName("Previous Page").build(), () -> {
                    page = Math.max(0, page - 1);
                    Sounds.interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
                    open();
                }));
            elements.put(17, new ItemGuiElement(Labels.create(Items.PAPER).withName("Page %s/%s".formatted(page + 1, maxPage + 1)).build(), () -> {}));
            if (page < maxPage)
                elements.put(26, new ItemGuiElement(Labels.create(Items.LIME_CONCRETE).withName("Next Page").build(), () -> {
                    page = Math.min(maxPage, page + 1);
                    Sounds.interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
                    open();
                }));
        } else { // single page
            var index = 0;
            for (var entry : entries) {
                elements.put(index++, new ItemGuiElement(entry.getValue(), () -> callback.accept(entry.getKey())));
            }
        }

        player.openMenu(EditorUtils.make9x5(Component.literal("Select an option"), elements));
    }
}
