package red.jackf.jsst.features.portablecrafting;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.commands.CommandSourceStack;
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
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Feature;

public class PortableCrafting extends Feature<PortableCrafting.Config> {
    public static final TagKey<Item> CRAFTING_TABLES = TagKey.create(Registries.ITEM, JSST.id("crafting_tables"));

    private static MenuProvider getProvider(InteractionHand hand, Component title) {
        return new SimpleMenuProvider(((i, inventory, player) -> {
            final CraftingMenu menu = new CraftingMenu(i, player.getInventory(), ContainerLevelAccess.create(player.level, player.getOnPos()));
            ((JSSTInventoryItemValidable) menu).jsst_setItemValidation(hand);
            return menu;
        }), title);
    }

    @Override
    public void init() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!this.getConfig().enabled) return InteractionResultHolder.pass(ItemStack.EMPTY);
            if (getConfig().sneakOnly && !player.isCrouching()) return InteractionResultHolder.pass(ItemStack.EMPTY);
            final var stack = player.getItemInHand(hand);
            if (stack.is(CRAFTING_TABLES)) {
                player.openMenu(getProvider(hand, stack.getHoverName()));
                return InteractionResultHolder.success(ItemStack.EMPTY);
            } else {
                return InteractionResultHolder.pass(ItemStack.EMPTY);
            }
        });
    }

    @Override
    public String id() {
        return "portableCrafting";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().portableCrafting;
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(OptionBuilders.withBoolean("sneakOnly", () -> getConfig().sneakOnly, newValue -> getConfig().sneakOnly = newValue));
    }

    public static class Config extends Feature.Config {
        @Comment("Whether players need to sneak to use crafting tables as items. (Default: false, Options: true, false)")
        public boolean sneakOnly = false;
    }
}
