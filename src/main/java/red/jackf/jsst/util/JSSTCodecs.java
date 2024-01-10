package red.jackf.jsst.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

public class JSSTCodecs {
    public static final Codec<ItemStack> SIMPLIFIED_ITEMSTACK = JFLCodecs.firstInList(
            BuiltInRegistries.ITEM.byNameCodec().flatComapMap(Item::getDefaultInstance, stack -> {
                if (stack.hasTag() || stack.getCount() != 1) {
                    return DataResult.error(() -> "Not a simple ItemStack");
                } else {
                    return DataResult.success(stack.getItem());
                }
            }), ItemStack.CODEC);
}
