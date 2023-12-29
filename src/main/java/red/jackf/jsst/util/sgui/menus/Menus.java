package red.jackf.jsst.util.sgui.menus;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.selector.PaginatedSelectorMenu;
import red.jackf.jsst.util.sgui.menus.selector.SinglePageSelectorMenu;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Menus {
    // colour predicates
    private static final Pattern SMALL_HEX = Pattern.compile("^#([\\da-fA-F]{3})$");
    private static final Pattern HEX = Pattern.compile("^#([\\da-fA-F]{6})$");
    private static final Pattern COMMA_SEPARATED = Pattern.compile("(?<red>\\d{1,3}) ?, ?(?<green>\\d{1,3}) ?, ?(?<blue>\\d{1,3})");
    private static final Component SMALL_HEX_EXAMPLE = Component.literal("#§cF§aF§9F");
    private static final Component HEX_EXAMPLE = Component.literal("#§cFF§aFF§9FF");
    private static final Component COMMA_SEPARATED_EXAMPLE = Component.literal("§c255§r, §a255§r, §9255");
    private static final Component INTEGER = Component.literal("16777215");
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
                .predicate(ResourceLocation::isValidResourceLocation)
                .createAndShow(opt -> onFinish.accept(opt.map(ResourceLocation::new)));
    }

    private static Optional<Colour> parseColour(String str) {
        str = str.strip();

        var smallHexMatch = SMALL_HEX.matcher(str);
        if (smallHexMatch.matches()) {
            try {
                String hexStr = smallHexMatch.group(1);
                var stretched = String.valueOf(hexStr.charAt(0)) + hexStr.charAt(0) +
                        hexStr.charAt(1) + hexStr.charAt(1) +
                        hexStr.charAt(2) + hexStr.charAt(2);
                int raw = Integer.parseUnsignedInt(stretched, 16);
                return Optional.of(Colour.fromInt(0xFF_000000 | raw));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }

        var hexMatch = HEX.matcher(str);
        if (hexMatch.matches()) {
            try {
                String hexStr = hexMatch.group(1);
                int raw = Integer.parseUnsignedInt(hexStr, 16);
                return Optional.of(Colour.fromInt(0xFF_000000 | raw));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }

        var commaMatch = COMMA_SEPARATED.matcher(str);
        if (commaMatch.matches()) {
            return Optional.of(Colour.fromRGB(
                    Integer.parseInt(commaMatch.group("red")),
                    Integer.parseInt(commaMatch.group("green")),
                    Integer.parseInt(commaMatch.group("blue"))
            ));
        }

        try {
            return Optional.of(Colour.fromInt(0xFF_000000 | Integer.parseInt(str)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static void colour(
            ServerPlayer player,
            Consumer<Optional<Colour>> onFinish) {
        stringBuilder(player)
                .title(Component.translatable("jsst.itemEditor.colour.custom"))
                .initial("#")
                .hint(s -> {
                    var colour = parseColour(s);
                    if (colour.isPresent()) {
                        return GuiElementBuilder.from(new ItemStack(Items.GLOWSTONE))
                                                .setName(Component.literal("|".repeat(30)).withColor(colour.get().toARGB()))
                                                .addLoreLine(Component.translatable("jsst.itemEditor.colour.custom.hint"))
                                                .addLoreLine(SMALL_HEX_EXAMPLE)
                                                .addLoreLine(HEX_EXAMPLE)
                                                .addLoreLine(COMMA_SEPARATED_EXAMPLE)
                                                .addLoreLine(INTEGER)
                                                .build();
                    } else {
                        return GuiElementBuilder.from(new ItemStack(Items.GRAY_CONCRETE))
                                                .setName(Component.translatable("jsst.common.invalid").setStyle(Styles.NEGATIVE))
                                                .addLoreLine(Component.translatable("jsst.itemEditor.colour.custom.hint"))
                                                .addLoreLine(SMALL_HEX_EXAMPLE)
                                                .addLoreLine(HEX_EXAMPLE)
                                                .addLoreLine(COMMA_SEPARATED_EXAMPLE)
                                                .addLoreLine(INTEGER)
                                                .build();
                    }
                })
                .predicate(s -> parseColour(s).isPresent())
                .createAndShow(s -> onFinish.accept(s.flatMap(Menus::parseColour)));
    }
}
