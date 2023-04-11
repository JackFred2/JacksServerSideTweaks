package red.jackf.jsst.features.itemeditor.utils;

import java.util.function.Consumer;

public interface CancellableCallback<T> {
    void accept(T result);
    void cancel();

    static <T> CancellableCallback<T> of(Consumer<T> accept, Runnable cancel) {
        return new CancellableCallback<>() {
            @Override
            public void accept(T result) {
                accept.accept(result);
            }

            @Override
            public void cancel() {
                cancel.run();
            }
        };
    }
}
