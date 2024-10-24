package red.jackf.jsst.impl.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface Callbacks {
    /**
     * Makes a consumer object single use, with no operation otherwise.
     */
    static <T> Consumer<T> singleUse(Consumer<T> callback) {
        AtomicBoolean ran = new AtomicBoolean(false);

        return t -> {
            if (ran.getAndSet(true)) return;

            callback.accept(t);
        };
    }
}
