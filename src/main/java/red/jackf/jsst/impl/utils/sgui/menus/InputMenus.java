package red.jackf.jsst.impl.utils.sgui.menus;

import com.mojang.serialization.DataResult;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface InputMenus {
    /**
     * Allows a user to input a string.
     */
    static StringInputMenu.Builder<String> string(ServerPlayer player) {
        return new StringInputMenu.Builder<>(player, DataResult::success);
    }

    /**
     * Allows a user to input a resource location.
     */
    static StringInputMenu.Builder<ResourceLocation> resLoc(ServerPlayer player) {
        return new StringInputMenu.Builder<>(player, ResourceLocation::read);
    }

    /**
     * Allows a user to input an RGB colour (no alpha). Contains a hint showing examples and a preview on the output.
     */
    static StringInputMenu.Builder<Colour> colour(ServerPlayer player) {
        return new StringInputMenu.Builder<>(player, InputMenus::tryParseColour)
                .hintElement(getColourHint())
                .appendOutput((str, colour, builder) -> builder.setName(Component.literal(str).withColor(colour.toARGB())))
                .initial("0");
    }

    static StringInputMenu.Builder<Integer> integer(ServerPlayer player, @Nullable Integer minimum, @Nullable Integer maximum) {
        var builder = new StringInputMenu.Builder<>(player, s -> tryParseInteger(s, minimum, maximum));

        if (minimum != null && maximum != null) {
            builder.hint(Component.translatable("jsst.ui.input.minMax", minimum, maximum));
        } else if (minimum != null) {
            builder.hint(Component.translatable("jsst.ui.input.min", minimum));
        } else if (maximum != null) {
            builder.hint(Component.translatable("jsst.ui.input.max", maximum));
        }

        return builder;
    }

    /**
     * Allows a user to select an option out of an arbitrary list.
     */
    static <T> SelectionMenu.Builder<T> selection(ServerPlayer player) {
        return new SelectionMenu.Builder<>(player);
    }

    // internals

    private static DataResult<Integer> tryParseInteger(String intStr, @Nullable Integer minimum, @Nullable Integer maximum) {
        try {
            int parsed = Integer.parseInt(intStr);

            if (minimum != null && parsed < minimum) {
                return DataResult.error(() -> "Smaller than minimum");
            } else if (maximum != null && parsed > maximum) {
                return DataResult.error(() -> "Larger than maximum");
            } else {
                return DataResult.success(parsed);
            }

        } catch (NumberFormatException ex) {
            return DataResult.error(ex::getMessage);
        }
    }

    private static DataResult<Colour> tryParseHex(String hexStr) {
        return DataResult.success(Colour.fromInt(0xFF_000000 | Integer.parseUnsignedInt(hexStr, 16)));
    }

    private static DataResult<Colour> tryParseColour(String str) {
        str = str.strip();
        String condensed = str.toLowerCase().replace(" ", "");

        // x11
        if (ColourUtils.X11.containsKey(condensed)) {
            return DataResult.success(Colour.fromInt(0xFF_000000 | ColourUtils.X11.get(condensed)));
        }

        // #8bf
        Matcher smallHex = Pattern.compile("^#([0-9a-fA-F]{3})$").matcher(str);
        if (smallHex.find()) {
            // double characters (4d5 -> 44dd55)
            //noinspection SuspiciousRegexArgument
            String fullHex = smallHex.group(1).replaceAll(".", "$0$0");
            return tryParseHex(fullHex);
        }

        // #7fb0ff
        Matcher fullHex = Pattern.compile("^#([0-9a-fA-F]{6})$").matcher(str);
        if (fullHex.find()) {
            return tryParseHex(fullHex.group(1));
        }

        // 127, 192, 255
        Matcher commaSeparated = Pattern.compile("^(?<red>\\d{1,3}) ?[ ,] ?(?<green>\\d{1,3}) ?[ ,] ?(?<blue>\\d{1,3})$").matcher(str);
        if (commaSeparated.find()) {
            int r = Integer.parseInt(commaSeparated.group("red"));
            int g = Integer.parseInt(commaSeparated.group("green"));
            int b = Integer.parseInt(commaSeparated.group("blue"));
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) return DataResult.error(() -> "Colour out of bounds");
            return DataResult.success(Colour.fromRGB(r, g, b));
        }

        // raw integer
        try {
            return DataResult.success(Colour.fromInt(Integer.parseInt(str)));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Invalid number format");
        }
    }

    private static GuiElementInterface getColourHint() {
        return JSSTElementBuilder.from(Items.GLOWSTONE_DUST).ui()
                .setName(Component.translatable("jsst.itemEditor.validFormats"))
                .addLoreLine(Component.literal("- #§cRR§aGG§9BB"))
                .addLoreLine(Component.literal("- #§cR§aG§9B"))
                .addLoreLine(Component.literal("- §c127§r, §a191, §9255"))
                .addLoreLine(Component.literal("- 16777215"))
                .addLoreLine(Component.literal("- X11 Colour Name"))
                .build();
    }
}
