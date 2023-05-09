package red.jackf.jsst.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static red.jackf.jsst.command.CommandUtils.*;

public class OptionBuilders {
    private static Component success(String optName, String oldVal, String newVal) {
        return line(TextType.SUCCESS,
                variable(optName),
                symbol(": "),
                variable(oldVal),
                symbol(" -> "),
                variable(newVal));
    }

    private static Component display(String optName, String value) {
        return line(TextType.INFO,
                variable(optName),
                symbol(": "),
                variable(value));
    }

    private static Component unchanged(String optName, String value) {
        return line(TextType.INFO,
                variable(optName),
                symbol(": "),
                variable(value),
                text(" (unchanged) "));
    }

    @SuppressWarnings("unused")
    private static Component fail(String optName) {
        return line(TextType.ERROR,
                text("Error updating "),
                variable(optName),
                text("check console/logs."));
    }

    /**
     * Adds an enum's options to a node; using each value's {@link StringRepresentable#getSerializedName()} as labels.
     *
     * @param name      Label for this option.
     * @param enumClass Class for the enum; used to get all values.
     * @param getter    Should return the option's current value.
     * @param setter    Called when a value is <i>changed</i>. This should set the new value in the config, and to update any world state.
     * @param <E>       Enum type to use options for; should be {@link StringRepresentable}
     * @return Created node for the option; use {@link com.mojang.brigadier.builder.ArgumentBuilder#then(ArgumentBuilder)} to add to a node.
     */
    public static <E extends Enum<E> & StringRepresentable> LiteralArgumentBuilder<CommandSourceStack> withEnum(String name, Class<E> enumClass, Supplier<E> getter, Consumer<E> setter) {
        assert enumClass.isEnum();
        var node = literal(name);
        for (E value : enumClass.getEnumConstants()) {
            node.then(literal(value.getSerializedName()).executes(ctx -> {
                var oldValue = getter.get();
                if (oldValue == value) {
                    ctx.getSource().sendSuccess(unchanged(node.getLiteral(), value.getSerializedName()), false);
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
     * Adds a float range option to a node.
     *
     * @param name   Label for this option.
     * @param getter Should return the option's current value.
     * @param setter Called when a value is <i>changed</i>. This should set the new value in the config, and to update any world state.
     * @return Created node for the option; use {@link com.mojang.brigadier.builder.ArgumentBuilder#then(ArgumentBuilder)} to add to a node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> withFloatRange(String name, Float min, Float max, Supplier<Float> getter, Consumer<Float> setter) {
        var node = literal(name);
        node.then(argument(name, FloatArgumentType.floatArg(min, max)).executes(ctx -> {
            var oldValue = getter.get();
            var newValue = FloatArgumentType.getFloat(ctx, name);
            if (oldValue == newValue) {
                ctx.getSource().sendFailure(unchanged(node.getLiteral(), oldValue.toString()));
                return 0;
            } else {
                setter.accept(newValue);
                JSST.CONFIG.save();
                ctx.getSource()
                        .sendSuccess(success(node.getLiteral(), oldValue.toString(), String.valueOf(newValue)), true);
                return 1;
            }
        })).executes(ctx -> {
            ctx.getSource().sendSuccess(display(node.getLiteral(), getter.get().toString()), false);
            return 1;
        });
        return node;
    }

    /**
     * Adds an integer range option to a node.
     *
     * @param name   Label for this option.
     * @param getter Should return the option's current value.
     * @param setter Called when a value is <i>changed</i>. This should set the new value in the config, and to update any world state.
     * @return Created node for the option; use {@link com.mojang.brigadier.builder.ArgumentBuilder#then(ArgumentBuilder)} to add to a node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> withIntRange(String name, Integer min, Integer max, Supplier<Integer> getter, Consumer<Integer> setter) {
        var node = literal(name);
        node.then(argument(name, IntegerArgumentType.integer(min, max)).executes(ctx -> {
            var oldValue = getter.get();
            var newValue = IntegerArgumentType.getInteger(ctx, name);
            if (oldValue == newValue) {
                ctx.getSource().sendFailure(unchanged(node.getLiteral(), oldValue.toString()));
                return 0;
            } else {
                setter.accept(newValue);
                JSST.CONFIG.save();
                ctx.getSource()
                        .sendSuccess(success(node.getLiteral(), oldValue.toString(), String.valueOf(newValue)), true);
                return 1;
            }
        })).executes(ctx -> {
            ctx.getSource().sendSuccess(display(node.getLiteral(), getter.get().toString()), false);
            return 1;
        });
        return node;
    }

    /**
     * Adds a boolean option to a node
     *
     * @param name   Label for this option.
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
                ctx.getSource().sendSuccess(line(TextType.INFO,
                        text(feature.id()),
                        symbol(": "),
                        variable("enabled"),
                        text(" (unchanged)")), false);
                return 0;
            } else {
                ctx.getSource().sendSuccess(line(TextType.SUCCESS,
                        text(feature.id()),
                        symbol(": "),
                        variable("disabled"),
                        symbol(" -> "),
                        variable("enabled")), true);
                feature.getConfig().enabled = true;
                feature.onEnabled();
                JSST.CONFIG.save();
                return 1;
            }
        })).then(literal("disable").executes(ctx -> {
            if (!feature.getConfig().enabled) {
                ctx.getSource().sendSuccess(line(TextType.INFO,
                        text(feature.id()),
                        symbol(": "),
                        variable("disabled"),
                        text(" (unchanged)")), false);
                return 0;
            } else {
                ctx.getSource().sendSuccess(line(TextType.ERROR,
                        text(feature.id()),
                        symbol(": "),
                        variable("enabled"),
                        symbol(" -> "),
                        variable("disabled")), true);
                feature.getConfig().enabled = false;
                feature.onDisabled();
                JSST.CONFIG.save();
                return 1;
            }
        })).executes(ctx -> {
            if (feature.getConfig().enabled) {
                ctx.getSource().sendSuccess(line(TextType.SUCCESS,
                        text(feature.id()),
                        symbol(": "),
                        text("enabled")), false);
                return 1;
            } else {
                ctx.getSource().sendSuccess(line(TextType.ERROR,
                        text(feature.id()),
                        symbol(": "),
                        text("disabled")), false);
                return 0;
            }
        });
    }
}
