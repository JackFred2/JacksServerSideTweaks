package red.jackf.jsst.impl.feature.portablecrafting;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.mixinutils.JSSTItemValidation;

public class PortableCrafting {
    public static final TagKey<Item> TAG = TagKey.create(Registries.ITEM, JSST.id("portable_crafting"));

    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide && isValidCraftingTable(level.registryAccess(), player.getItemInHand(hand))) {
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

    public static boolean isValidCraftingTable(RegistryAccess registryAccess, ItemStack stack) {
        return TagUtil.isIn(registryAccess, TAG, stack.getItem());
    }
}
