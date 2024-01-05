package red.jackf.jsst.util;

import java.util.Objects;
import java.util.function.Function;

public final class Result<T> {
    private static final Result<?> EMPTY = new Result<>(false, null);

    private final boolean hasResult;
    private final T result;

    private Result(boolean hasResult, T result) {
        this.hasResult = hasResult;
        this.result = result;
    }

    public static <T> Result<T> of(T value) {
        return new Result<>(true, value);
    }

    public static <T> Result<T> empty() {
        //noinspection unchecked
        return (Result<T>) EMPTY;
    }

    public boolean hasResult() {
        return hasResult;
    }

    public T result() {
        if (!hasResult) throw new IllegalArgumentException("No result to get!");
        return result;
    }

    public <U> Result<U> map(Function<T, U> mapping) {
        if (this.hasResult()) {
            return Result.of(mapping.apply(this.result));
        } else {
            return empty();
        }
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> mapping) {
        if (this.hasResult()) {
            return mapping.apply(this.result);
        } else {
            return empty();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Result<?>) obj;
        return this.hasResult == that.hasResult &&
                Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasResult, result);
    }

    @Override
    public String toString() {
        return this == EMPTY ? "EmptyResult" : "Result[" + result + ']';
    }
}
