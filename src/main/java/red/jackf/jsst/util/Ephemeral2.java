package red.jackf.jsst.util;

import org.jetbrains.annotations.Nullable;

public class Ephemeral2<T>  {
    @Nullable
    private T value = null;
    private int count = 0;

    public void push(T value) {
        this.push(value, 1);
    }

    public void push(T value, int count) {
        if (Math.max(0, count) == 0) return;
        this.value = value;
        this.count = count;
    }

    public boolean hasValue() {
        return this.count > 0;
    }

    public T pop() {
        if (count == 0) throw new IllegalArgumentException("No value available - use .hasValue()");
        return popNullable();
    }

    public @Nullable T popNullable() {
        if (count == 0) return null;
        this.count--;
        if (count == 0) {
            @Nullable T value = this.value;
            this.value = null;
            return value;
        } else {
            return value;
        }
    }
}
