package red.jackf.jsst.util.sgui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public interface Styles {
    Style LABEL = Style.EMPTY.withColor(ChatFormatting.WHITE);
    Style LIST_ITEM = Style.EMPTY.withColor(ChatFormatting.GRAY);
    Style ID = Style.EMPTY.withColor(ChatFormatting.YELLOW);
}
