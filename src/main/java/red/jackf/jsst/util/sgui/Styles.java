package red.jackf.jsst.util.sgui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public interface Styles {
    Style LABEL = Style.EMPTY.withColor(ChatFormatting.WHITE);
    Style MINOR_LABEL = Style.EMPTY.withColor(ChatFormatting.GRAY);
    Style ID = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY); // copying F3 + h
    Style VARIABLE = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
    Style INPUT_HINT = Style.EMPTY.withColor(ChatFormatting.GREEN);
    Style INPUT_DECOR = Style.EMPTY.withColor(ChatFormatting.AQUA);
    Style INPUT_KEY = Style.EMPTY.withColor(ChatFormatting.WHITE);
    Style EXAMPLE = Style.EMPTY.withColor(ChatFormatting.GOLD);

    Style POSITIVE = Style.EMPTY.withColor(0x7FFF7F);
    Style INFO = Style.EMPTY.withColor(0x7F7FFF);
    Style NEGATIVE = Style.EMPTY.withColor(0xFF7F7F);
}
