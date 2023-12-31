package red.jackf.jsst.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class JSSTCodecs {
    public static final Codec<ItemStack> SIMPLIFIED_ITEMSTACK = firstOf(
            BuiltInRegistries.ITEM.byNameCodec().flatComapMap(Item::getDefaultInstance, stack -> {
                if (stack.hasTag() || stack.getCount() != 1) {
                    return DataResult.error(() -> "Not a simple ItemStack");
                } else {
                    return DataResult.success(stack.getItem());
                }
            }), ItemStack.CODEC);

    @SafeVarargs
    public static <T> Codec<T> firstOf(Codec<T>... codecs) {
        return new FirstOfCodec<>(codecs);
    }

    // Alternative to ExtraCodecs.withAlternative that tries both decode options since they have the same type
    public static final class FirstOfCodec<T> implements Codec<T> {
        private final Codec<T>[] codecs;

        @SafeVarargs
        private FirstOfCodec(Codec<T>... codecs) {
            this.codecs = codecs;
        }

        @Override
        public <A> DataResult<Pair<T, A>> decode(DynamicOps<A> ops, A input) {
            for (Codec<T> codec : this.codecs) {
                DataResult<Pair<T, A>> data = codec.decode(ops, input);
                if (data.result().isPresent()) {
                    return data;
                }
            }
            return DataResult.error(() -> "No Codecs can decode " + input);
        }

        @Override
        public <A> DataResult<A> encode(T input, DynamicOps<A> ops, A prefix) {
            for (Codec<T> codec : this.codecs) {
                DataResult<A> data = codec.encode(input, ops, prefix);
                if (data.result().isPresent()) {
                    return data;
                }
            }

            return DataResult.error(() -> "No Codecs can encode " + input);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final FirstOfCodec<?> eitherCodec = ((FirstOfCodec<?>) o);
            return Arrays.equals(codecs, eitherCodec.codecs);
        }

        @Override
        public int hashCode() {
            return Objects.hash((Object) codecs);
        }

        @Override
        public String toString() {
            return "FirstOfCodec" + Arrays.toString(codecs);
        }
    }
}
