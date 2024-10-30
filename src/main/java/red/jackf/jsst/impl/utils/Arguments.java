package red.jackf.jsst.impl.utils;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Validators for numeric arguments
 */
public interface Arguments {
    /**
     * minInclusive <= value <= maxInclusive, otherwise throws IllegalArgumentException(message)
     */
    static void inRange(int value, int minInclusive, int maxInclusive, String message, Object... args) {
        if (value < minInclusive || value > maxInclusive)
            throw makeException(message, value, args);
    }

    /**
     * a >= b, otherwise throws IllegalArgumentException(message)
     */
    static void isGreaterOrEq(int a, int b, String message, Object... args) {
        if (a < b)
            throw makeException(message, a, b, args);
    }

    /**
     *a <= b, otherwise throws IllegalArgumentException(message)
     */
    static void isLessOrEq(int a, int b, String message, Object... args) {
        if (a > b)
            throw makeException(message, a, b, args);
    }

    private static IllegalArgumentException makeException(String message, int value, Object... args) {
        return new IllegalArgumentException(message.formatted(args, ArrayUtils.addFirst(args, value)));
    }
}
