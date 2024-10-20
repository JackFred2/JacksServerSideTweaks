package red.jackf.jsst.impl.feature.itemeditor;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class Result {
    private static final Result EMPTY = new Result(false, null);

    private final boolean hasResult;
    private final ItemStack stack;

    private Result(boolean hasResult, ItemStack stack) {
        this.hasResult = hasResult;
        this.stack = stack;
    }

    public static Result empty() {
        return EMPTY;
    }

    public static Result of(ItemStack stack) {
        return new Result(true, stack);
    }

    public boolean hasResult() {
        return hasResult;
    }

    public ItemStack result() {
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Result) obj;
        return this.hasResult == that.hasResult &&
                Objects.equals(this.stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasResult, stack);
    }

    @Override
    public String toString() {
        return "Result[" +
                "hasResult=" + hasResult + ", " +
                "stack=" + stack + ']';
    }
}
