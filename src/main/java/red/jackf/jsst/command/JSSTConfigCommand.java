package red.jackf.jsst.command;

import blue.endless.jankson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jsst.JSST;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.config.JSSTJankson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class JSSTConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignored, Commands.CommandSelection commandSelection) {
        var root = literal("jsst").requires(stack -> stack.hasPermission(4));

        addQuery(root, "JSST Config", JSST.CONFIG_HANDLER::get);

        var reload = literal("reload").executes(ctx -> {
            JSST.CONFIG_HANDLER.load();
            var text = style("Configuration reloaded. ", YELLOW);
            text.append(style("Click to print.", Style.EMPTY.withColor(LIGHT_PURPLE)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jsst"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/jsst")))));
            sendLoud(ctx, text);
            return 1;
        });

        root.then(reload);

        var portableCraftingTable = literal("portableCraftingTable");
        addQuery(portableCraftingTable, "Portable Crafting Table", () -> JSST.CONFIG_HANDLER.get().portableCrafting);

        addEnabled(portableCraftingTable, "Portable Crafting Table", (enabled) -> {
            JSST.CONFIG_HANDLER.get().portableCrafting.enabled = enabled;
            JSST.CONFIG_HANDLER.save();
        });

        var pctMode = literal("mode");
        addEnum(pctMode, "PCT Mode", mode -> {
            JSST.CONFIG_HANDLER.get().portableCrafting.mode = mode;
            JSST.CONFIG_HANDLER.save();
        }, JSSTConfig.PortableCrafting.Mode.class);

        var pctItems = literal("items");
        addCollection(pctItems, "PCT Items", ResourceLocationArgument.id(), rl -> {
                JSST.CONFIG_HANDLER.get().portableCrafting.items.add(rl);
                JSST.CONFIG_HANDLER.save();
            },
            (ctx, builder) -> SharedSuggestionProvider.suggest(Registry.ITEM.keySet().stream()
                .filter(rl -> !JSST.CONFIG_HANDLER.get().portableCrafting.items.contains(rl))
                .map(ResourceLocation::toString), builder),
            rl -> {
                JSST.CONFIG_HANDLER.get().portableCrafting.items.remove(rl);
                JSST.CONFIG_HANDLER.save();
            },
            (ctx, builder) -> SharedSuggestionProvider.suggest(JSST.CONFIG_HANDLER.get().portableCrafting.items.stream()
                .map(ResourceLocation::toString), builder),
            ResourceLocation.class);

        portableCraftingTable.then(pctMode);
        portableCraftingTable.then(pctItems);

        root.then(portableCraftingTable);

        dispatcher.register(root);
    }

    private static MutableComponent style(String text, ChatFormatting format) {
        return Component.literal(text).withStyle(format);
    }

    private static MutableComponent style(String text, Style style) {
        return Component.literal(text).withStyle(style);
    }

    private static void sendQuiet(CommandContext<CommandSourceStack> ctx, Component... text) {
        if (text.length == 0) return;
        var textOut = text[0].copy();
        for (int i = 1; i < text.length; i++) textOut.append(text[i].copy());
        ctx.getSource().sendSuccess(textOut, false);
    }

    private static void sendLoud(CommandContext<CommandSourceStack> ctx, Component... text) {
        if (text.length == 0) return;
        var textOut = text[0].copy();
        for (int i = 1; i < text.length; i++) textOut.append(text[i].copy());
        ctx.getSource().sendSuccess(textOut, true);
    }

    /**
     * Serialize a JsonElement to a List<Component> for use in chat.
     */
    private static void serializeJson(List<Component> list, Component prefix, JsonElement element, int indentation, boolean flatten, boolean isRoot) {
        var str = style(" ".repeat(indentation), WHITE).append(prefix);
        if (element instanceof JsonNull) {
            list.add(str.append(style("null", RED)));
            return;
        } else if (element instanceof JsonPrimitive jsonPrimitive) {
            MutableComponent valueComponent;
            var clazz = jsonPrimitive.getValue().getClass();
            if (clazz == Boolean.class) {
                valueComponent = style(jsonPrimitive.asString(), jsonPrimitive.asBoolean(false) ? GREEN : RED);
            } else if (Number.class.isAssignableFrom(clazz)) {
                valueComponent = style(jsonPrimitive.asString(), LIGHT_PURPLE);
            } else if (clazz == String.class) {
                var strSplit = jsonPrimitive.asString().split("\n");
                valueComponent = style(strSplit[0], WHITE);
                for (int i = 1; i < strSplit.length; i++) {
                    valueComponent.append(style("\\n", Style.EMPTY.withColor(TextColor.fromRgb(0xFF7700))));
                    valueComponent.append(style(strSplit[i], WHITE));
                }
            } else {
                valueComponent = style(jsonPrimitive.asString(), AQUA);
            }
            list.add(str.append(valueComponent));
            return;
        } else if (element instanceof JsonObject jsonObject) {
            if (!flatten) list.add(str);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (isRoot && entry.getKey().equals("enabled")) continue; // handled in the main title
                var objPrefix = style(entry.getKey(), YELLOW).append(style(": ", WHITE));
                if (jsonObject.getComment(entry.getKey()) != null) {
                    objPrefix.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, style(jsonObject.getComment(entry.getKey()), WHITE))));
                }
                serializeJson(list, objPrefix, entry.getValue(), indentation + (flatten ? 0 : 2), false, false);
            }
            return;
        } else if (element instanceof JsonArray jsonArray) {
            list.add(str);
            for (JsonElement listItem : jsonArray) {
                var list2 = new ArrayList<Component>();
                serializeJson(list2, style("", WHITE), listItem, 0, true, false);
                if (list2.size() == 0) continue;
                list.add(style(" ".repeat(indentation) + "  - ", WHITE).append(list2.get(0)));
                for (int i = 1; i < list2.size(); i++) {
                    list.add(style(" ".repeat(indentation) + "    ", WHITE).append(list2.get(i)));
                }
            }
            return;
        }

        throw new IllegalStateException("Unknown JsonElement passed");
    }

    private static <T> void addQuery(LiteralArgumentBuilder<CommandSourceStack> node, String title, Supplier<T> configPart) {
        T part = configPart.get();
        var titleComponent = style(title, YELLOW).append(style(": ", WHITE));
        var disabledComponent = style("(", WHITE).append(style("disabled", RED)).append(style(")", WHITE));
        var enabledComponent = style("(", WHITE).append(style("enabled", GREEN)).append(style(")", WHITE));

        node.executes(ctx -> {
            if (JSSTJankson.INSTANCE.toJson(part) instanceof JsonObject json) {
                var enabled = json.get(Boolean.class, "enabled");
                if (enabled != null) {
                    sendQuiet(ctx, titleComponent, enabled ? enabledComponent : disabledComponent);
                } else {
                    sendQuiet(ctx, titleComponent);
                }
                var list = new ArrayList<Component>();
                serializeJson(list, style("", WHITE), json, 2, true, true);
                for (Component component : list) {
                    sendQuiet(ctx, component);
                }
                return 1;
            } else {
                return 0;
            }
        });
    }

    private static <E extends Enum<E>> void addEnum(LiteralArgumentBuilder<CommandSourceStack> node, String title, Consumer<E> setter, Class<E> clazz) {
        var startText = style(title, YELLOW).append(style(" is now ", WHITE));
        var dotText = style(".", WHITE);

        for (E val : clazz.getEnumConstants()) {
            node.then(literal(val.name()).executes(ctx -> {
                setter.accept(val);
                sendLoud(ctx, startText, style(val.name(), AQUA), dotText);
                return 1;
            }));
        }
    }

    private static <T> void addCollection(LiteralArgumentBuilder<CommandSourceStack> node, String title,
                                          ArgumentType<T> argType,
                                          Consumer<T> adder, SuggestionProvider<CommandSourceStack> adderSuggestions,
                                          Consumer<T> remover, SuggestionProvider<CommandSourceStack> removerSuggestions,
                                          Class<T> clazz) {
        node.then(literal("add").then(argument("value", argType).suggests(adderSuggestions).executes(ctx -> {
            T arg = ctx.getArgument("value", clazz);
            adder.accept(arg);
            sendLoud(ctx, style("Added ", WHITE).append(style(arg.toString(), AQUA)).append(style(" to ", WHITE)).append(style(title, YELLOW)));
            return 1;
        })));

        node.then(literal("remove").then(argument("value", argType).suggests(removerSuggestions).executes(ctx -> {
            T arg = ctx.getArgument("value", clazz);
            remover.accept(arg);
            sendLoud(ctx, style("Removed ", WHITE).append(style(arg.toString(), AQUA)).append(style(" from ", WHITE)).append(style(title, YELLOW)));
            return 1;
        })));
    }

    private static void addEnabled(LiteralArgumentBuilder<CommandSourceStack> node, String title, Consumer<Boolean> setter) {
        var startText = style(title, YELLOW).append(style(" is now ", WHITE));
        var dotText = style(".", WHITE);

        var enabledText = style("enabled", GREEN);
        var disabledText = style("disabled", RED);

        node.then(literal("enable").executes(ctx -> {
            setter.accept(true);
            sendLoud(ctx, startText, enabledText, dotText);
            return 1;
        })).then(literal("disable").executes(ctx -> {
            setter.accept(false);
            sendLoud(ctx, startText, disabledText, dotText);
            return 1;
        }));
    }
}
