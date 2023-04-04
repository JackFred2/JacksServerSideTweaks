package red.jackf.jsst.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class CommandUtils {
    public static MutableComponent successPrefix() {
        return Component.literal("[+] ").withStyle(ChatFormatting.DARK_GREEN);
    }

    public static MutableComponent infoPrefix() {
        return symbol("[-] ");
    }

    public static MutableComponent errorPrefix() {
        return Component.literal("[x] ").withStyle(ChatFormatting.DARK_RED);
    }

    public static MutableComponent symbol(String contents) {
        return Component.literal(contents).withStyle(ChatFormatting.YELLOW);
    }

    public static MutableComponent variable(String contents) {
        return Component.literal(contents).withStyle(ChatFormatting.AQUA);
    }

    public static MutableComponent text(String contents) {
        return Component.literal(contents).withStyle(ChatFormatting.WHITE);
    }

    public static Style suggests(String command) {
        return Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }
}
