package red.jackf.jsst.impl.utils.sgui.menus.selection;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.utils.Callbacks;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SelectionMenu<T> extends SimpleGuiExt {
    protected final List<T> options;
    protected final Function<T, GuiElementInterface> labelFactory;
    private final Consumer<Optional<T>> callback;

    SelectionMenu(MenuType<?> menu, Component title, ServerPlayer player, List<T> options, Function<T, GuiElementInterface> labelFactory, Consumer<Optional<T>> callback) {
        super(menu, player, false);
        this.options = options;
        this.labelFactory = labelFactory;
        this.callback = Callbacks.singleUse(callback);
        this.setTitle(title);

        this.drawStatic();
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    protected void complete(T option) {
        this.callback.accept(Optional.of(option));
    }

    protected void cancel() {
        this.callback.accept(Optional.empty());
    }

    public static <T> Builder<T> builder(ServerPlayer player) {
        return new Builder<>(player);
    }

    public static class Builder<T> {
        private final ServerPlayer player;
        private List<T> options = null;
        private Function<T, GuiElementInterface> labelFactory = null;
        private Component title = Component.empty();
        @Nullable
        private BiPredicate<T, String> filter = null;

        public Builder(ServerPlayer player) {
            this.player = player;
        }

        public Builder<T> options(Collection<T> options) {
            this.options = List.copyOf(options);
            return this;
        }

        public Builder<T> options(Stream<T> options) {
            this.options = options.toList();
            return this;
        }

        public Builder<T> title(Component title) {
            this.title = title;
            return this;
        }

        public Builder<T> labelStacks(Function<T, ItemStack> stackLabelFactory) {
            this.labelFactory = t -> JSSTElementBuilder.from(stackLabelFactory.apply(t)).build();
            return this;
        }

        public Builder<T> labelElements(Function<T, GuiElementInterface> labelFactory) {
            this.labelFactory = labelFactory;
            return this;
        }

        public Builder<T> filterable(BiPredicate<T, String> filter) {
            this.filter = filter;
            return this;
        }

        public void start(Consumer<Optional<T>> callback) {
            Objects.requireNonNull(options, "No Options");
            Objects.requireNonNull(labelFactory, "No Labeller");

            if (options.size() <= 52 && filter == null) {
                new SinglePageSelectionMenu<>(player, title, options, labelFactory, callback).open();
            } else {
                new MultiPageSelectionMenu<>(player, title, options, labelFactory, filter, callback).open();
            }
        }
    }
}
