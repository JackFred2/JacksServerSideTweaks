package red.jackf.jsst.features.portablecraftingtable;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.config.JSSTConfig;

public class PortableCraftingTable {
    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var config = JSST.CONFIG_HANDLER.get();
            if (JSST.CONFIG_HANDLER.get().portableCrafting.enabled
                && player instanceof ServerPlayer serverPlayer
                && (config.portableCrafting.mode == JSSTConfig.PortableCrafting.Mode.always || player.isCrouching())) {
                var heldItem = serverPlayer.getItemInHand(hand);
                if (config.portableCrafting.items.contains(Registry.ITEM.getKey(heldItem.getItem()))) {
                    player.openMenu(new SimpleMenuProvider((i, inventory, ignored) -> {
                        var menu = new CraftingMenu(i, inventory, ContainerLevelAccess.create(level, serverPlayer.blockPosition()));
                        ((JSSTAlwaysValidable) menu).jsst_setAlwaysValid(hand);
                        return menu;
                    }, heldItem.getHoverName()));
                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }
}
