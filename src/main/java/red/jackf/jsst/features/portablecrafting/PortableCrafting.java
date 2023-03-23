package red.jackf.jsst.features.portablecrafting;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

public class PortableCrafting implements Feature {
    public static final TagKey<Item> CRAFTING_TABLES = TagKey.create(Registries.ITEM, JSST.id("crafting_tables"));

    private static MenuProvider getProvider(InteractionHand hand, Component title) {
        return new SimpleMenuProvider(((i, inventory, player) -> {
            final CraftingMenu menu = new CraftingMenu(i, player.getInventory(), ContainerLevelAccess.create(player.level, player.getOnPos()));
            ((JSSTInventoryItemValidable) menu).setItemValidation(hand);
            return menu;
        }), title);
    }

    @Override
    public void init() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            final var stack = player.getItemInHand(hand);
            if (stack.is(CRAFTING_TABLES)) {
                player.openMenu(getProvider(hand, stack.getHoverName()));
                return InteractionResultHolder.success(ItemStack.EMPTY);
            } else {
                return InteractionResultHolder.pass(ItemStack.EMPTY);
            }
        });
    }
}
