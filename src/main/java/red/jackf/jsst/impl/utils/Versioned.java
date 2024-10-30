package red.jackf.jsst.impl.utils;

//? if <=1.21.1 {
/*import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
*///?} else
import net.minecraft.world.InteractionResult;

public interface Versioned {
    //? if <=1.21.1 {
    /*static InteractionResultHolder<ItemStack> itemInteractPass() {
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    *///?} else {
    static InteractionResult itemInteractPass() {
        return InteractionResult.PASS;
    }
    //?}
}