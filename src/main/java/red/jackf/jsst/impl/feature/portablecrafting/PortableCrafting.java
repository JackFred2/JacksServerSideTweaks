package red.jackf.jsst.impl.feature.portablecrafting;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.mixinutils.JSSTItemValidatedMenu;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Versioned;

public class PortableCrafting {
    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            JSSTConfig.PortableCrafting config = JSSTConfig.INSTANCE.instance().portableCrafting;

            if (!level.isClientSide
                    && config.enabled
                    && (player.isShiftKeyDown() || !config.requiresSneak)
                    && isValidCraftingTable(level.registryAccess(), player.getItemInHand(hand))) {
                openMenuForPlayer(player, (ServerLevel) level, hand);
            }

            return Versioned.itemInteractPass();
        });
    }

    private static void openMenuForPlayer(Player player, ServerLevel level, InteractionHand hand) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, _player) -> {
                    CraftingMenu menu = new CraftingMenu(containerId, inventory, ContainerLevelAccess.create(level, _player.blockPosition()));
                    ((JSSTItemValidatedMenu) menu).jsst$markAsItemValidation(hand);
                    return menu;
                },
                player.getItemInHand(hand).getHoverName()
        ));
    }

    public static boolean isValidCraftingTable(RegistryAccess access, ItemStack stack) {
        String config = JSSTConfig.INSTANCE.instance().portableCrafting.itemIdOrTag;

        return stack.is(RegistryUtils.getValuesFromIDOrTag(access, Registries.ITEM, config));
    }
}
