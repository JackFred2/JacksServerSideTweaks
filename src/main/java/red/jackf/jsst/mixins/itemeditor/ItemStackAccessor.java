package red.jackf.jsst.mixins.itemeditor;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {

    @Invoker("getHideFlags")
    int jsst$itemEditor$getTooltipHideMask();
}
