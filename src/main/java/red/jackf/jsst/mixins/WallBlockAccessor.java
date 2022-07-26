package red.jackf.jsst.mixins;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Provides access to {@link WallBlock#connectsTo(BlockState, boolean, Direction)}. Done for compatibility with any mod
 * that overrides connectsTo in their WallBlock.
 */
@Mixin(WallBlock.class)
public interface WallBlockAccessor {

    @Invoker("connectsTo")
    boolean jsst_connectsTo(BlockState state, boolean sideSolid, Direction direction);
}
