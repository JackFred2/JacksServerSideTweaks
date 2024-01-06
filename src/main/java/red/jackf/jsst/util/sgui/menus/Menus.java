package red.jackf.jsst.util.sgui.menus;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.selector.PaginatedSelectorMenu;
import red.jackf.jsst.util.sgui.menus.selector.SinglePageSelectorMenu;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Menus {
    private static final int PAGINATION_THRESHOLD = 52;

    // colour
    private static final Pattern SMALL_HEX = Pattern.compile("^#([\\da-fA-F]{3})$");
    private static final Pattern HEX = Pattern.compile("^#([\\da-fA-F]{6})$");
    private static final Pattern COMMA_SEPARATED = Pattern.compile("(?<red>\\d{1,3}) ?, ?(?<green>\\d{1,3}) ?, ?(?<blue>\\d{1,3})");
    private static final Component SMALL_HEX_EXAMPLE = Component.literal("#§cF§aF§9F");
    private static final Component HEX_EXAMPLE = Component.literal("#§cFF§aFF§9FF");
    private static final Component COMMA_SEPARATED_EXAMPLE = Component.literal("§c255§r, §a255§r, §9255");
    private static final Component INTEGER_EXAMPLE = Component.literal("16777215");

    // resource location
    private static final ItemStack DEFAULT_RESLOC_HINT = GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
            .setName(Component.translatable("jsst.itemEditor.menus.resLocHint", Component.literal("namespace:path").setStyle(Styles.EXAMPLE)))
            .asStack();

    // duration
    private static final Pattern INFINITE = Pattern.compile("^i|inf|infinite|∞$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TICKS = Pattern.compile("^\\+?(?<ticks>\\d+) ?(?:t|tick|ticks)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECONDS = Pattern.compile("^\\+?(?<seconds>\\d+([,.]\\d+)?) ?(?:s|sec|secs|second|seconds)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES = Pattern.compile("^\\+?(?<minutes>\\d+([,.]\\d+)?) ?(?:m|min|mins|minute|minutes)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOURS = Pattern.compile("^\\+?(?<hours>\\d+([,.]\\d+)?) ?(?:h|hr|hrs|hour|hours)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOCK = Pattern.compile("^((?<hours>\\d+):)?(?<minutes>\\d+):(?<seconds>\\d+)([,.](?<ticks>\\d+))?$", Pattern.CASE_INSENSITIVE);

    /**
     * Allows a user to select one of a collection of options. Will resize itself as needed, and will paginate. If paginated,
     * a search bar will also be available. Does not close itself; you'll need to do this in the callback.
     */
    public static <T> void selector(
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

    public static StringInputMenu.Builder stringBuilder(ServerPlayer player) {
        return new StringInputMenu.Builder(player);
    }

    private static Result<Integer> parseInt(String str, @Nullable Integer minimumInclusive, @Nullable Integer maximumInclusive) {
        try {
            int parsed = Integer.parseInt(str);
            if (minimumInclusive != null && parsed < minimumInclusive) return Result.empty();
            if (maximumInclusive != null && parsed > maximumInclusive) return Result.empty();
            return Result.of(parsed);
        } catch (NumberFormatException ignored) {
            return Result.empty();
        }
    }

    private static ItemStack makeIntegerHint(@Nullable Integer minimumInclusive, @Nullable Integer maximumInclusive) {
        if (minimumInclusive == null) {
            if (maximumInclusive == null) {
                return ItemStack.EMPTY;
            } else {
                return GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                        .setName(Component.literal("x ≤ " + maximumInclusive).withStyle(Styles.EXAMPLE))
                        .asStack();
            }
        } else {
            if (maximumInclusive == null) {
                return GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                                        .setName(Component.literal(minimumInclusive + " ≤ x").withStyle(Styles.EXAMPLE))
                                        .asStack();
            } else {
                return GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                                        .setName(Component.literal(minimumInclusive + " ≤ x ≤ " + maximumInclusive).withStyle(Styles.EXAMPLE))
                                        .asStack();
            }
        }
    }

    public static void integer(
            ServerPlayer player,
            Component title,
            int initial,
            @Nullable Integer minimumInclusive,
            @Nullable Integer maximumInclusive,
            @Nullable ItemStack hint,
            Consumer<Result<Integer>> callback) {
        stringBuilder(player)
                .title(title)
                .initial(String.valueOf(initial))
                .predicate(s -> parseInt(s, minimumInclusive, maximumInclusive).hasResult())
                .hint(hint == null ? makeIntegerHint(minimumInclusive, maximumInclusive) : hint)
                .createAndShow(result -> callback.accept(result.flatMap(s -> parseInt(s, minimumInclusive, maximumInclusive))));
    }

    public static void resourceLocation(
            ServerPlayer player,
            Component title,
            ResourceLocation initial,
            @Nullable ItemStack hint,
            Consumer<Result<ResourceLocation>> callback) {
        stringBuilder(player)
                .title(title)
                .initial(initial.toString())
                .hint(hint == null ? DEFAULT_RESLOC_HINT : hint)
                .predicate(ResourceLocation::isValidResourceLocation)
                .createAndShow(opt -> callback.accept(opt.map(ResourceLocation::new)));
    }

    private static Result<Colour> parseColour(String str) {
        str = str.strip();

        var smallHexMatch = SMALL_HEX.matcher(str);
        if (smallHexMatch.matches()) {
            try {
                String hexStr = smallHexMatch.group(1);
                var stretched = String.valueOf(hexStr.charAt(0)) + hexStr.charAt(0) +
                        hexStr.charAt(1) + hexStr.charAt(1) +
                        hexStr.charAt(2) + hexStr.charAt(2);
                int raw = Integer.parseUnsignedInt(stretched, 16);
                return Result.of(Colour.fromInt(0xFF_000000 | raw));
            } catch (NumberFormatException ignored) {
                return Result.empty();
            }
        }

        var hexMatch = HEX.matcher(str);
        if (hexMatch.matches()) {
            try {
                String hexStr = hexMatch.group(1);
                int raw = Integer.parseUnsignedInt(hexStr, 16);
                return Result.of(Colour.fromInt(0xFF_000000 | raw));
            } catch (NumberFormatException ignored) {
                return Result.empty();
            }
        }

        var commaMatch = COMMA_SEPARATED.matcher(str);
        if (commaMatch.matches()) {
            return Result.of(Colour.fromRGB(
                    Integer.parseInt(commaMatch.group("red")),
                    Integer.parseInt(commaMatch.group("green")),
                    Integer.parseInt(commaMatch.group("blue"))
            ));
        }

        try {
            return Result.of(Colour.fromInt(0xFF_000000 | Integer.parseInt(str)));
        } catch (NumberFormatException ignored) {
            return Result.empty();
        }
    }

    public static void customColour(
            ServerPlayer player,
            Consumer<Result<Colour>> callback) {
        stringBuilder(player)
                .title(Component.translatable("jsst.itemEditor.colour.custom"))
                .initial("#")
                .hint(s -> {
                    var colour = parseColour(s);
                    if (colour.hasResult()) {
                        return GuiElementBuilder.from(new ItemStack(Items.GLOWSTONE))
                                                .setName(Component.literal("|".repeat(30)).withColor(colour.result().toARGB()))
                                                .addLoreLine(Component.translatable("jsst.itemEditor.colour.custom.hint"))
                                                .addLoreLine(SMALL_HEX_EXAMPLE)
                                                .addLoreLine(HEX_EXAMPLE)
                                                .addLoreLine(COMMA_SEPARATED_EXAMPLE)
                                                .addLoreLine(INTEGER_EXAMPLE)
                                                .build();
                    } else {
                        return GuiElementBuilder.from(new ItemStack(Items.GRAY_CONCRETE))
                                                .setName(Component.translatable("jsst.common.invalid").setStyle(Styles.NEGATIVE))
                                                .addLoreLine(Component.translatable("jsst.itemEditor.colour.custom.hint"))
                                                .addLoreLine(SMALL_HEX_EXAMPLE)
                                                .addLoreLine(HEX_EXAMPLE)
                                                .addLoreLine(COMMA_SEPARATED_EXAMPLE)
                                                .addLoreLine(INTEGER_EXAMPLE)
                                                .build();
                    }
                })
                .predicate(s -> parseColour(s).hasResult())
                .createAndShow(result -> callback.accept(result.flatMap(Menus::parseColour)));
    }

    private static Result<Integer> parseDuration(String str, double tickrate, boolean allowInfinite) {
        str = str.strip().toLowerCase();

        if (allowInfinite && INFINITE.matcher(str).matches()) {
            return Result.of(Integer.MAX_VALUE);
        }

        var ticksMatch = TICKS.matcher(str);
        if (ticksMatch.matches()) {
            return Result.of(Integer.parseUnsignedInt(ticksMatch.group("ticks")));
        }

        var secondsMatch = SECONDS.matcher(str);
        if (secondsMatch.matches()) {
            double seconds = Double.parseDouble(secondsMatch.group("seconds"));
            if (Double.isFinite(seconds)) {
                return Result.of((int) (seconds * tickrate));
            } else {
                return Result.empty();
            }
        }

        var minutesMatch = MINUTES.matcher(str);
        if (minutesMatch.matches()) {
            double minutes = Double.parseDouble(minutesMatch.group("minutes"));
            if (Double.isFinite(minutes)) {
                return Result.of((int) (minutes * tickrate * 60));
            } else {
                return Result.empty();
            }
        }

        var hourMatch = HOURS.matcher(str);
        if (hourMatch.matches()) {
            double hours = Double.parseDouble(hourMatch.group("hours"));
            if (Double.isFinite(hours)) {
                return Result.of((int) (hours * tickrate * 60 * 60));
            } else {
                return Result.empty();
            }
        }

        var clockMatch = CLOCK.matcher(str);
        if (clockMatch.matches()) {
            int hours = clockMatch.group("hours") != null ? Integer.parseUnsignedInt(clockMatch.group("hours")) : 0;
            int minutes = Integer.parseUnsignedInt(clockMatch.group("minutes"));
            int seconds = Integer.parseUnsignedInt(clockMatch.group("seconds"));
            int ticks = clockMatch.group("ticks") != null ? Integer.parseUnsignedInt(clockMatch.group("ticks")) : 0;

            int total = ticks
                    + (int) (seconds * tickrate)
                    + (int) (minutes * tickrate * 60)
                    + (int) (hours * tickrate * 60 * 60);

            return Result.of(total);
        }

        return Result.empty();
    }

    /**
     * Gets a duration in ticks. Can optionally allow infinite values, by returning {@link Integer#MAX_VALUE}
     */
    public static void duration(
            ServerPlayer player,
            Component title,
            String initial,
            boolean allowInfinite,
            Consumer<Result<Integer>> callback) {
        stringBuilder(player)
                .title(title)
                .initial(initial)
                .predicate(s -> parseDuration(s, player.server.tickRateManager().tickrate(), allowInfinite).hasResult())
                .createAndShow(result -> callback.accept(result.flatMap(s -> parseDuration(s, player.server.tickRateManager().tickrate(), allowInfinite))));
    }
}
