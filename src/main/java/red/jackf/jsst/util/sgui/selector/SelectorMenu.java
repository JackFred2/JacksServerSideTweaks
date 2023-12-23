package red.jackf.jsst.util.sgui.selector;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.util.sgui.LabelMap;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public abstract class SelectorMenu<T> extends SimpleGui {
    protected final List<T> options;
    private final Consumer<Selection<T>> onSelect;
    protected final LabelMap<T> labelMap;
    private boolean hasRanCallback = false;

    public SelectorMenu(MenuType<?> type, ServerPlayer player, Collection<T> options, Consumer<Selection<T>> onSelect, LabelMap<T> labelMap) {
        super(type, player, false);
        this.options = List.copyOf(options);
        this.onSelect = onSelect;
        this.labelMap = labelMap;
    }

    protected void finish(Selection<T> selection) {
        if (hasRanCallback) return;
        this.hasRanCallback = true;
        this.onSelect.accept(selection);
    }

    @Override
    public void onClose() {
        if (!hasRanCallback) {
            hasRanCallback = true;
            this.onSelect.accept(new Selection<>(false, null));
        }
    }

    public record Selection<T>(boolean hasResult, T result) {}
}
