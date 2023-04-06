package red.jackf.jsst.command;

import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import red.jackf.jsst.features.Feature;

import java.util.List;
import java.util.function.Supplier;

public final class CommandUtils {
    private CommandUtils() {}

    /**
     * Returns a green success prefix.
     */
    private static MutableComponent successPrefix() {
        return Component.literal("[+] ").withStyle(ChatFormatting.DARK_GREEN);
    }

    private static MutableComponent infoPrefix() {
        return Component.literal("[•] ").withStyle(ChatFormatting.YELLOW);
    }

    private static MutableComponent errorPrefix() {
        return Component.literal("[-] ").withStyle(ChatFormatting.DARK_RED);
    }

    public static Text symbol(String contents) {
        return new Text.Symbol(contents);
    }

    public static Text variable(String contents) {
        return variable(contents, Style.EMPTY);
    }

    public static Text variable(String contents, Style style) {
        return new Text.Plain(contents, style.withColor(ChatFormatting.AQUA));
    }

    public static Text text(String contents) {
        return text(contents, Style.EMPTY);
    }

    public static Text text(String contents, Style style) {
        return new Text.Plain(contents, style.withColor(ChatFormatting.WHITE));
    }

    // Suggests a command when clicked
    public static Style suggests(String command) {
        return Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    // Joins a list of Components with a prefix
    public static Component line(TextType type, Text... text) {
        var line = type.prefixGetter.get();
        for (Text other : text) line.append(other.resolve(type));
        return line;
    }

    // Joins a list of Components with a prefix
    public static Component line(TextType type, List<Text> text) {
        return line(type, text.toArray(new Text[0]));
    }

    // Joins a list of Components without a prefix
    public static Component list(TextType type, Text... text) {
        var line = Component.literal(""); // empty to prevent style mixing
        for (Text other : text) line.append(other.resolve(type));
        return line;
    }

    // Joins a list of Components with a prefix
    public static Component list(TextType type, List<Text> text) {
        return list(type, text.toArray(new Text[0]));
    }

    // Wrapper around a brigadier command that fails if the feature is not enabled.
    public static EnabledWrapper wrapper(Feature<?> feature) {
        return command -> ctx -> {
            if (!feature.getConfig().enabled) {
                ctx.getSource().sendFailure(line(TextType.ERROR, text("Feature "), variable(feature.id()), text(" not enabled!")));
            }
            return command.run(ctx);
        };
    }

    public interface EnabledWrapper {
        Command<CommandSourceStack> wrap(Command<CommandSourceStack> command);
    }

    public enum TextType {
        SUCCESS(CommandUtils::successPrefix, ChatFormatting.GREEN),
        INFO(CommandUtils::infoPrefix, ChatFormatting.YELLOW),
        ERROR(CommandUtils::errorPrefix, ChatFormatting.RED);

        private final Supplier<MutableComponent> prefixGetter;
        private final ChatFormatting punctuationColour;

        TextType(Supplier<MutableComponent> prefixGetter, ChatFormatting punctuationColour) {
            this.prefixGetter = prefixGetter;
            this.punctuationColour = punctuationColour;
        }
    }

    public interface Text {
        MutableComponent resolve(TextType type);

        record Plain(String text, Style style) implements Text {
            @Override
            public MutableComponent resolve(TextType type) {
                return Component.literal(text).withStyle(style);
            }
        }

        record Symbol(String text) implements Text {
            @Override
            public MutableComponent resolve(TextType type) {
                return Component.literal(text).withStyle(type.punctuationColour);
            }
        }
    }
}
