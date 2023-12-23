package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.SlotHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiUtil {
    public static void returnItems(ServerPlayer player, Container container) {
        if (!player.isAlive() || player.hasDisconnected()) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
        } else {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }
        }
    }

    public static int slot(int column, int row) {
        return row * 9 + column;
    }

    public static void fill(SlotHolder holder, ItemStack stack, int colFrom, int colTo, int rowFrom, int rowTo) {
        for (int col = colFrom; col < colTo; col++) {
            for (int row = rowFrom; row < rowTo; row++) {
                holder.setSlot(slot(col, row), stack);
            }
        }
    }
}
