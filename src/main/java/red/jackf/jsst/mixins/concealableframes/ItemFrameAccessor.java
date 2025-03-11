package red.jackf.jsst.mixins.concealableframes;

import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameAccessor {

    @Accessor("fixed")
    boolean jsst$isFixed();
}
