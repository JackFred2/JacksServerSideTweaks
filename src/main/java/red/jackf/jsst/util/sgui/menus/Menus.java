package red.jackf.jsst.util.sgui.menus;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.selector.PaginatedSelectorMenu;
import red.jackf.jsst.util.sgui.menus.selector.SinglePageSelectorMenu;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class Menus {
    private static final int PAGINATION_THRESHOLD = 52;
    private static final ItemStack DEFAULT_RESLOC_HINT = GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
            .setName(Component.translatable("jsst.itemEditor.menus.resLocHint", Component.literal("namespace:path").setStyle(Styles.EXAMPLE)))
            .asStack();

    /**
     * Allows a user to select one of a collection of options. Will resize itself as needed, and will paginate. If paginated,
     * a search bar will also be available. Does not close itself; you'll need to do this in the callback.
     */
    public static <T> void selector(
            ServerPlayer player,
            Component title,
            Collection<T> options,
            LabelMap<T> labelMap,
            Consumer<PaginatedSelectorMenu.Selection<T>> onSelect) {
        if (options.size() > PAGINATION_THRESHOLD) {
            new PaginatedSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        } else {
            new SinglePageSelectorMenu<>(player, title, options, onSelect, labelMap).open();
        }
    }

    public static StringInputMenu.Builder stringBuilder(ServerPlayer player) {
        return new StringInputMenu.Builder(player);
    }

    public static void resourceLocation(
            ServerPlayer player,
            Component title,
            ResourceLocation initial,
            @Nullable ItemStack hint,
            Consumer<Optional<ResourceLocation>> onFinish) {
        stringBuilder(player)
                .title(title)
                .initial(initial.toString())
                .hint(hint == null ? DEFAULT_RESLOC_HINT : hint)
                .createAndShow(opt -> onFinish.accept(opt.map(ResourceLocation::new)));
    }
}
