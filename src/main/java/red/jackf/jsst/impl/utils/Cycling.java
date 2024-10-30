package red.jackf.jsst.impl.utils;

import java.util.List;

/**
 * Methods for cycling through lists and enums
 */
public interface Cycling {
    static <T> T next(List<T> options, T current) {
        int currentIndex = options.indexOf(current);
        if (currentIndex == -1) return current;
        if (currentIndex == options.size() - 1) {
            return options.getFirst();
        } else {
            return options.get(currentIndex + 1);
        }
    }

    static <T> T previous(List<T> options, T current) {
        int currentIndex = options.indexOf(current);
        if (currentIndex == -1) return current;
        if (currentIndex == 0) {
            return options.getLast();
        } else {
            return options.get(currentIndex - 1);
        }
    }
}
