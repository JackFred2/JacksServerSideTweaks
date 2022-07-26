package red.jackf.jsst.features.portableshulkerbox;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import red.jackf.jsst.JSST;
import red.jackf.jsst.config.JSSTConfig;

public class PortableShulkerBox {
    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var config = JSST.CONFIG_HANDLER.get();
            if (JSST.CONFIG_HANDLER.get().portableShulkerBox.enabled
                && player instanceof ServerPlayer serverPlayer
                && (config.portableShulkerBox.mode == JSSTConfig.PortableShulkerBox.Mode.always || player.isCrouching())) {
                var heldItem = serverPlayer.getItemInHand(hand);
                if (heldItem.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    player.openMenu(new SimpleMenuProvider((containerId, inventory, ignored) -> {
                        var container = new SimpleContainer(27);
                        var loadedItems = getItemListTag(heldItem);
                        for (int slot = 0; slot < 27; slot++) {
                            var stack = loadedItems.get(slot);
                            if (stack.isEmpty()) continue;
                            container.setItem(slot, stack);
                        }
                        var menu = new ShulkerBoxMenu(containerId, inventory, container);

                        menu.addSlotListener(new Listener(serverPlayer, menu.slots.stream().filter(slot -> slot.getItem() == heldItem).findFirst().get().index, heldItem));
                        return menu;
                    }, heldItem.getHoverName()));
                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }

    private static NonNullList<ItemStack> getItemListTag(ItemStack heldItem) {
        var items = NonNullList.withSize(27, ItemStack.EMPTY);

        var tag = heldItem.getTag();
        if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            var beTag = tag.getCompound("BlockEntityTag");
            ContainerHelper.loadAllItems(beTag, items);
        }
        return items;
    }

    private static final class Listener implements ContainerListener {
        private final ServerPlayer serverPlayer;
        private final int slot;
        private final ItemStack shulkerBox;

        private Listener(ServerPlayer player, int slot, ItemStack shulkerBox) {
            this.serverPlayer = player;
            this.slot = slot;
            this.shulkerBox = shulkerBox;
        }

        @Override
        public void slotChanged(AbstractContainerMenu containerToSend, int slotChanged, ItemStack stack) {
            if (slotChanged == slot && containerToSend.getSlot(slot).getItem() != shulkerBox) {
                serverPlayer.closeContainer();
                return;
            }


            if (slotChanged < 27) {
                var box = containerToSend.slots.get(slot).getItem();
                var tag = box.getOrCreateTag();

                var itemTag = new CompoundTag();
                ContainerHelper.saveAllItems(itemTag, containerToSend.getItems());
                tag.put("BlockEntityTag", itemTag);
                box.setTag(tag);
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

        }
    }
}
