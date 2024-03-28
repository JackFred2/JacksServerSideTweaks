package red.jackf.jsst.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface AfterPlacePlaceBlock {
    Event<AfterPlacePlaceBlock> EVENT = EventFactory.createArrayBacked(AfterPlacePlaceBlock.class, listeners -> (player, level, pos, state, blockStack) -> {
        for (AfterPlacePlaceBlock listener : listeners) {
            listener.afterPlayerPlaceBlock(player, level, pos, state, blockStack);
        }
    });

    void afterPlayerPlaceBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, ItemStack blockStack);
}
