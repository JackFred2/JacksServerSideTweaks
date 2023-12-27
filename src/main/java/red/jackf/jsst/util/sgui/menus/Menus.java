package red.jackf.jsst.util.sgui.menus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.selector.PaginatedSelectorMenu;
import red.jackf.jsst.util.sgui.menus.selector.SinglePageSelectorMenu;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class Menus {
    private static final int PAGINATION_THRESHOLD = 52;

    /**
     * Allows a user to select one of a collection of options. Will resize itself if needed, and will paginate. If paginated,
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

    public static void string(
            ServerPlayer player,
            Component title,
            String initial,
            @Nullable ItemStack hint,
            Consumer<Optional<String>> onFinish) {
        string(player,
               title,
               initial,
               new ItemStack(Items.BARRIER).setHoverName(Component.literal(initial).withStyle(ChatFormatting.WHITE)),
               hint,
               new ItemStack(Items.EMERALD).setHoverName(Component.empty().withStyle(ChatFormatting.WHITE)),
               onFinish);
    }

    public static void string(
            ServerPlayer player,
            Component title,
            String initial,
            ItemStack input,
            @Nullable ItemStack hint,
            ItemStack output,
            Consumer<Optional<String>> onFinish) {
        new StringInputMenu(player, title, initial, input, hint, output, onFinish).open();
    }
}
