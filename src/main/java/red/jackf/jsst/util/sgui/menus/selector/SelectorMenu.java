package red.jackf.jsst.util.sgui.menus.selector;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed abstract class SelectorMenu<T> extends SimpleGui permits PaginatedSelectorMenu, SinglePageSelectorMenu {
    private static final int PAGINATION_THRESHOLD = 52;

    protected final List<T> options;
    private final Consumer<Result<T>> onSelect;
    protected final LabelMap<T> labelMap;
    protected final @Nullable FilterInstance<T> filter;
    private boolean hasRanCallback = false;

    protected SelectorMenu(MenuType<?> type, Component title, ServerPlayer player, Collection<T> options, Filter<T> filter, LabelMap<T> labelMap, Consumer<Result<T>> callback) {
        super(type, player, false);
        this.setTitle(title);
        this.options = List.copyOf(options);
        this.filter = filter == null ? null : new FilterInstance<>(filter);
        this.labelMap = labelMap;
        this.onSelect = callback;
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
            Consumer<Result<T>> callback) {
        SelectorMenu.<T>builder(player)
                .title(title)
                .options(options)
                .labelMap(labelMap)
                .createAndShow(callback);
    }

    protected void finish(Result<T> selection) {
        if (!hasRanCallback) {
            this.hasRanCallback = true;
            this.onSelect.accept(selection);
        }
    }

    protected List<T> getOptions() {
        if (this.filter == null || !this.filter.active()) return this.options;
        return this.options.stream().filter(this.filter.filter().predicate).toList();
    }

    public static <T> Builder<T> builder(ServerPlayer player) {
        return new Builder<>(player);
    }

    @Override
    public void onClose() {
        finish(Result.empty());
    }

    public static class Builder<T> {
        private final ServerPlayer player;
        private Component title = Translations.select();
        private final List<T> options = new ArrayList<>();
        private @Nullable Filter<T> filter = null;
        private LabelMap<T> labelMap = null;

        private Builder(ServerPlayer player) {
            this.player = player;
        }

        public Builder<T> title(Component title) {
            this.title = title;
            return this;
        }

        public Builder<T> option(T option) {
            this.options.add(option);
            return this;
        }

        public Builder<T> options(Collection<T> options) {
            this.options.addAll(options);
            return this;
        }

        public Builder<T> options(T[] options) {
            this.options.addAll(Arrays.asList(options));
            return this;
        }

        public Builder<T> options(Stream<T> options) {
            options.forEachOrdered(this.options::add);
            return this;
        }

        public Builder<T> filter(Supplier<ToggleButton.Builder> filterSupplier, Predicate<T> predicate) {
            this.filter = new Filter<>(filterSupplier, predicate);
            return this;
        }

        public Builder<T> labelMap(LabelMap<T> labelMap) {
            this.labelMap = labelMap;
            return this;
        }

        public void createAndShow(Consumer<Result<T>> callback) {
            Objects.requireNonNull(this.labelMap);
            if (options.isEmpty()) throw new IllegalArgumentException("No options");

            int threshold = PAGINATION_THRESHOLD;
            if (filter != null) threshold -= 1;
            if (options.size() > threshold) {
                new PaginatedSelectorMenu<>(player, title, options, filter, labelMap, callback).open();
            } else {
                new SinglePageSelectorMenu<>(player, title, options, filter, labelMap, callback).open();
            }
        }
    }

    protected record Filter<T>(Supplier<ToggleButton.Builder> buttonBuilder, Predicate<T> predicate) {}

    protected static class FilterInstance<T> {
        private final Filter<T> filter;
        private boolean active = false;

        protected FilterInstance(Filter<T> filter) {
            this.filter = filter;
        }

        public Filter<T> filter() {
            return filter;
        }

        public boolean active() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
