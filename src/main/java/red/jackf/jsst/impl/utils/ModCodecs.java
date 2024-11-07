package red.jackf.jsst.impl.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

public interface ModCodecs {
    Codec<ItemStack> SIMPLE_ONLY_STACK = ItemStack.SIMPLE_ITEM_CODEC.flatComapMap(stack -> stack, stack -> {
        if (stack.getComponentsPatch().isEmpty() && stack.getCount() == 1) {
            return DataResult.success(stack);
        } else {
            return DataResult.error(() -> "Not a simple stack");
        }
    });

    Codec<ItemStack> POSSIBLY_SIMPLE_STACK = JFLCodecs.firstInList(SIMPLE_ONLY_STACK, ItemStack.CODEC);
}
