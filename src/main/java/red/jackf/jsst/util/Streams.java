package red.jackf.jsst.util;

import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

public class Streams {
    public static <T> Comparator<T> comparingComponent(Function<T, Component> keyExtractor) {
        return Comparator.comparing(t -> keyExtractor.apply(t).getString().toLowerCase(Locale.ROOT));
    }
}
