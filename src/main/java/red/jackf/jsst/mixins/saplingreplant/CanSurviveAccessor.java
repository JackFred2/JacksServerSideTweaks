package red.jackf.jsst.mixins.saplingreplant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
//? if <=1.21.4
/*import net.minecraft.world.level.block.BushBlock;*/
//? if >=1.21.5
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//? if <=1.21.4 {
/*@Mixin(BushBlock.class)
*///?} else
@Mixin(VegetationBlock.class)
public interface CanSurviveAccessor {

    @Invoker
    boolean invokeCanSurvive(BlockState state, LevelReader level, BlockPos pos);
}
