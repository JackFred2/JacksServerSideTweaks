package red.jackf.jsst.mixins.saplingreplant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BushBlock.class)
public interface BushBlockAccessor {

    @Invoker
    boolean invokeCanSurvive(BlockState state, LevelReader level, BlockPos pos);
}
