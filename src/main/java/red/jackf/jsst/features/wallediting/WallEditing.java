package red.jackf.jsst.features.wallediting;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.phys.HitResult;
import red.jackf.jsst.JSST;
import red.jackf.jsst.mixins.WallBlockAccessor;

import java.util.HashMap;
import java.util.Map;

public class WallEditing {
    public static void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            var config = JSST.CONFIG_HANDLER.get();
            if (JSST.CONFIG_HANDLER.get().wallEditing.enabled && player instanceof ServerPlayer serverPlayer) {
                var heldItem = serverPlayer.getItemInHand(hand);
                if (heldItem.getItem() == Items.STICK && hitResult.getType() == HitResult.Type.BLOCK) {
                    var state = level.getBlockState(hitResult.getBlockPos());
                    if (state.getBlock() instanceof WallBlockAccessor wallBlock) {
                        var newState = state;
                        Map<Direction, Boolean> connected = new HashMap<>(4);

                        for (Direction direction : Direction.Plane.HORIZONTAL) {
                            var neighbourPos = hitResult.getBlockPos().relative(direction);
                            var neighbourState = level.getBlockState(neighbourPos);
                            connected.put(direction, wallBlock.jsst_connectsTo(neighbourState,
                                neighbourState.isFaceSturdy(level, neighbourPos, direction.getOpposite()),
                                direction.getOpposite()));
                        }

                        if ((connected.get(Direction.EAST) && connected.get(Direction.WEST))
                            || (connected.get(Direction.NORTH) && connected.get(Direction.SOUTH))) {
                            newState = newState.cycle(WallBlock.UP);
                        }

                        if (!newState.equals(state)) {
                            level.setBlock(hitResult.getBlockPos(), newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}
