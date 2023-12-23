package red.jackf.jsst.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class JSSTCodecs {
    public static final Codec<ItemStack> SIMPLIFIED_ITEMSTACK = ExtraCodecs.withAlternative(
            BuiltInRegistries.ITEM.byNameCodec().flatComapMap(ItemStack::new, stack -> {
                if (stack.hasTag() || stack.getCount() != 1) {
                    return DataResult.error(() -> "Not a simple ItemStack");
                } else {
                    return DataResult.success(stack.getItem());
                }
            }), ItemStack.CODEC);
}
