package red.jackf.jsst.features;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.config.JSSTConfig;

public class PortableCraftingTable {
    public static void setup() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (JSST.CONFIG_HANDLER.get().portableCrafting.enabled) {
                var config = JSST.CONFIG_HANDLER.get();
                if (player instanceof ServerPlayer serverPlayer && (config.portableCrafting.mode == JSSTConfig.PortableCrafting.Mode.always || player.isCrouching())) {
                    var heldItem = serverPlayer.getItemInHand(hand);
                    if (config.portableCrafting.items.contains(Registry.ITEM.getKey(heldItem.getItem()))) {
                        player.openMenu(new SimpleMenuProvider((i, inventory, ignored) -> new CraftingMenu(i, inventory), heldItem.getHoverName()));
                        return InteractionResultHolder.success(ItemStack.EMPTY);
                    }
                }
            }
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }
}
