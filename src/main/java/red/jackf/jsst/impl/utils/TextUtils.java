package red.jackf.jsst.impl.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.GradientBuilder;

import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public interface TextUtils {
    static Component formatReslocOrTag(String input) {
        if (!isValidReslocOrTag(input)) {
            return literal(input).withStyle(ChatFormatting.RED);
        }
        MutableComponent result = Component.empty();
        if (input.charAt(0) == '#') {
            result.append(literal("#").withStyle(ChatFormatting.AQUA));
            input = input.substring(1);
        }
        result.append(literal(input).withStyle(ChatFormatting.YELLOW));
        return result;
    }

    static boolean isValidReslocOrTag(String input) {
        if (input.isEmpty()) return false;
        if (input.charAt(0) == '#') input = input.substring(1);
        return ResourceLocation.read(input).isSuccess();
    }

    static Component copyNoStyle(Component component) {
        MutableComponent root = component.plainCopy();

        for (Component sibling : component.getSiblings()) {
            root.append(copyNoStyle(sibling));
        }

        return root;
    }

    static Component applyGradient(String string, Style style, Gradient colour) {
        MutableComponent base = Component.empty().withStyle(style);

        for (int i = 0; i < string.length(); i++) {
            float progress = Math.min(((float) i) / Math.max(1, string.length() - 1), GradientBuilder.END);

            base.append(literal(String.valueOf(string.charAt(i))).withColor(colour.sample(progress).toARGB()));
        }

        return base;
    }

    static Component previewGradient(Gradient gradient) {
        return applyGradient("|".repeat(40), Style.EMPTY, gradient);
    }

    static Component formatResloc(String input) {
        if (ResourceLocation.tryParse(input) != null) {
            return literal(input).withStyle(ChatFormatting.YELLOW);
        } else {
            return literal(input).withStyle(ChatFormatting.RED);
        }
    }

    Function<Integer, Component> BLOCK_FORMAT = i -> {
        if (i == 1) {
            return translatable("jsst.ui.format.block");
        } else {
            return translatable("jsst.ui.format.blocks", i);
        }
    };

    Function<Integer, Component> TICK_FORMAT = i -> {
        if (i == 1) {
            return translatable("jsst.ui.format.tick");
        } else {
            return translatable("jsst.ui.format.ticks", i);
        }
    };
}
