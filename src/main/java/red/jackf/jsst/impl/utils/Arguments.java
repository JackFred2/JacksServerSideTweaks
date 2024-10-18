package red.jackf.jsst.impl.utils;

import org.apache.commons.lang3.ArrayUtils;

public interface Arguments {
    static void inRange(int value, int minInclusive, int maxInclusive, String message, Object... args) {
        if (value < minInclusive || value > maxInclusive)
            throw makeException(message, value, args);
    }

    static void isLessOrEq(int a, int b, String message, Object... args) {
        if (a > b)
            throw makeException(message, a, b, args);
    }

    private static IllegalArgumentException makeException(String message, int value, Object... args) {
        return new IllegalArgumentException(message.formatted(args, ArrayUtils.addFirst(args, value)));
    }
}
