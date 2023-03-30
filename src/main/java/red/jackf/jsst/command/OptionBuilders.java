package red.jackf.jsst.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OptionBuilders {
    private static Component success(String optName, String oldVal, String newVal) {
        return Component.literal("[+] ").withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(optName).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(oldVal).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(newVal).withStyle(ChatFormatting.WHITE));
    }

    private static MutableComponent display(String optName, String value) {
        return Component.literal("[-] ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(optName).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private static Component unchanged(String optName, String value) {
        return display(optName, value).append(Component.literal(" (unchanged) ").withStyle(ChatFormatting.YELLOW));
    }

    @SuppressWarnings("unused")
    private static Component fail(String optName) {
        return Component.literal("[x] ").withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal(optName).withStyle(ChatFormatting.WHITE))
                .append(": error; check console/logs").withStyle(ChatFormatting.RED);
    }

    /**
     * Adds an enum option to a node; using each value's {@link StringRepresentable#getSerializedName()} as labels.
     * @param name Label for this option.
     * @param enumClass Class for the enum; used to get all values.
     * @param getter Should return the option's current value.
     * @param setter Called when a value is <i>changed</i>. This should set the new value in the config, and to update any world state.
     * @return Created node for the option; use {@link com.mojang.brigadier.builder.ArgumentBuilder#then(ArgumentBuilder)} to add to a node.
     * @param <E> Enum type to use options for; should be {@link StringRepresentable}
     */
    public static <E extends Enum<E> & StringRepresentable> LiteralArgumentBuilder<CommandSourceStack> withEnum(String name, Class<E> enumClass, Supplier<E> getter, Consumer<E> setter) {
        assert enumClass.isEnum();
        var node = literal(name);
        for (E value : enumClass.getEnumConstants()) {
            node.then(literal(value.getSerializedName()).executes(ctx -> {
                var oldValue = getter.get();
                if (oldValue == value) {
                    ctx.getSource().sendFailure(unchanged(node.getLiteral(), value.getSerializedName()));
                    return 0;
                } else {
                    setter.accept(value);
                    JSST.CONFIG.save();
                    ctx.getSource().sendSuccess(success(node.getLiteral(), oldValue.getSerializedName(), value.getSerializedName()), true);
                    return 1;
                }
            }));
        }

        node.executes(ctx -> {
            ctx.getSource().sendSuccess(display(node.getLiteral(), getter.get().getSerializedName()), false);
            return 1;
        });

        return node;
    }

    /**
     * Adds a boolean option to a node
     * @param name Label for this option.
     * @param getter Should return the option's current value.
     * @param setter Called when a value is <i>changed</i>. This should set the new value in the config, and to update any world state.
     * @return Created node for the option; use {@link com.mojang.brigadier.builder.ArgumentBuilder#then(ArgumentBuilder)} to add to a node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> withBoolean(String name, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        var node = literal(name);
        node.then(argument(name, BoolArgumentType.bool()).executes(ctx -> {
            var oldValue = getter.get();
            var newValue = (Boolean) BoolArgumentType.getBool(ctx, name);
            if (oldValue == newValue) {
                ctx.getSource().sendFailure(unchanged(node.getLiteral(), newValue.toString()));
                return 0;
            } else {
                setter.accept(newValue);
                JSST.CONFIG.save();
                ctx.getSource().sendSuccess(success(node.getLiteral(), oldValue.toString(), newValue.toString()), true);
                return 1;
            }
        })).executes(ctx -> {
            ctx.getSource().sendSuccess(display(node.getLiteral(), getter.get().toString()), false);
            return 1;
        });
        return node;
    }

    static void addEnabled(LiteralArgumentBuilder<CommandSourceStack> base, Feature<?> feature) {
        base.then(literal("enable").executes(ctx -> {
            if (feature.getConfig().enabled) {
                ctx.getSource().sendFailure(Component.literal("[-] ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("enabled").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (unchanged)").withStyle(ChatFormatting.YELLOW)));
                return 0;
            } else {
                ctx.getSource().sendSuccess(Component.literal("[+] ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("disabled").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" -> ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("enabled").withStyle(ChatFormatting.WHITE)), true);
                feature.getConfig().enabled = true;
                feature.onEnabled();
                return 1;
            }
        })).then(literal("disable").executes(ctx -> {
            if (!feature.getConfig().enabled) {
                ctx.getSource().sendFailure(Component.literal("[-] ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("disabled").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (unchanged)").withStyle(ChatFormatting.YELLOW)));
                return 0;
            } else {
                ctx.getSource().sendSuccess(Component.literal("[-] ").withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.RED))
                        .append(Component.literal("enabled").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" -> ").withStyle(ChatFormatting.RED))
                        .append(Component.literal("disabled").withStyle(ChatFormatting.WHITE)), true);
                feature.getConfig().enabled = false;
                feature.onDisabled();
                return 1;
            }
        })).executes(ctx -> {
            if (feature.getConfig().enabled) {
                ctx.getSource().sendFailure(Component.literal("[+] ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("enabled").withStyle(ChatFormatting.WHITE)));
            } else {
                ctx.getSource().sendFailure(Component.literal("[x] ").withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal(feature.id()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.RED))
                        .append(Component.literal("disabled").withStyle(ChatFormatting.WHITE)));
            }
            return 1;
        });
    }
}
