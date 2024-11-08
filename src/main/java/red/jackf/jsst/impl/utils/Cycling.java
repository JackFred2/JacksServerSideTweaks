package red.jackf.jsst.impl.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Methods for cycling through lists and enums
 */
public interface Cycling {
    /**
     * Get element after current in the given list. If not found, return current.
     */
    static <T> T next(List<T> options, T current) {
        int currentIndex = options.indexOf(current);
        if (currentIndex == -1) return current;
        if (currentIndex == options.size() - 1) {
            return options.getFirst();
        } else {
            return options.get(currentIndex + 1);
        }
    }

    /**
     * Get element before current in the given list. If not found, return current.
     */
    static <T> T previous(List<T> options, T current) {
        int currentIndex = options.indexOf(current);
        if (currentIndex == -1) return current;
        if (currentIndex == 0) {
            return options.getLast();
        } else {
            return options.get(currentIndex - 1);
        }
    }

    /**
     * Return a new list containing the same elements, but cycled forward (i.e. element 0 becomes 1, last element becomes element 0, etc.)
     */
    static <T> List<T> shiftForward(List<T> list) {
        if (list.size() <= 1) return list;

        List<T> newList = new ArrayList<>(list.size());
        newList.add(list.getLast());
        newList.addAll(list.subList(0, list.size() - 1));

        return newList;
    }

    /**
     * Return a new list containing the same elements, but cycled backward (i.e. element 1 becomes 0, element 0 becomes last element, etc.)
     */
    static <T> List<T> shiftBackward(List<T> list) {
        if (list.size() <= 1) return list;

        List<T> newList = new ArrayList<>(list.size());
        newList.addAll(list.subList(1, list.size()));
        newList.add(list.getFirst());

        return newList;
    }
}
