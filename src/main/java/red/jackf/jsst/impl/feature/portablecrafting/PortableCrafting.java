package red.jackf.jsst.impl.feature.portablecrafting;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.mixinutils.JSSTItemValidation;

public class PortableCrafting {
    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide && isValidCraftingTable(player.getItemInHand(hand))) {
                openMenuForPlayer(player, (ServerLevel) level, hand);
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }

            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }

    private static void openMenuForPlayer(Player player, ServerLevel level, InteractionHand hand) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, _player) -> {
                    CraftingMenu menu = new CraftingMenu(containerId, inventory, ContainerLevelAccess.create(level, _player.blockPosition()));
                    ((JSSTItemValidation) menu).jsst$markAsItemValidation(hand);
                    return menu;
                },
                player.getItemInHand(hand).getHoverName()
        ));
    }

    public static boolean isValidCraftingTable(ItemStack stack) {
        return stack.is(Items.CRAFTING_TABLE);
    }
}
