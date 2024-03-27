package red.jackf.jsst.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

/**
 * Called after a player changes their equipped hotbar slot.
 */
public interface ChangeCarriedItem {
    Event<ChangeCarriedItem> EVENT = EventFactory.createArrayBacked(ChangeCarriedItem.class, invokers -> (player, oldSlot, newSlot) -> {
        for (ChangeCarriedItem listener : invokers) {
            listener.changeCarriedItem(player, oldSlot, newSlot);
        }
    });

    void changeCarriedItem(ServerPlayer player, int oldSlot, int newSlot);
}
