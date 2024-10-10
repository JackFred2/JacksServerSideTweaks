package red.jackf.jsst.impl.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.network.chat.Component.literal;

public interface StringUtils {
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
        //? if <=1.20.6 {
        /*return ResourceLocation.read(input).result().isPresent();
        *///?} else
        return ResourceLocation.read(input).isSuccess();
    }

    static ResourceLocation resloc(String raw) {
        //? if <=1.20.6 {
        /*return new ResourceLocation(raw);
        *///?} else
        return ResourceLocation.parse(raw);
    }
}
