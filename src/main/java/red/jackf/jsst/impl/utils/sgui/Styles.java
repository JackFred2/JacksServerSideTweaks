package red.jackf.jsst.impl.utils.sgui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public interface Styles {
    Style CLEAN = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);
    Style LABEL = Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY);
    Style MINOR_LABEL = Style.EMPTY.withItalic(false).withColor(ChatFormatting.DARK_GRAY);

    Style INPUT_HINT = CLEAN.withColor(ChatFormatting.GREEN);
    Style INPUT_DECOR = CLEAN.withColor(ChatFormatting.AQUA);
    Style INPUT_KEY = CLEAN.withColor(ChatFormatting.WHITE);

    Style POSITIVE = CLEAN.withColor(ChatFormatting.GREEN);
    Style NEGATIVE = CLEAN.withColor(ChatFormatting.RED);
}
