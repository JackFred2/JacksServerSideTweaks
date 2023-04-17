package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InventoryLockEditor extends Editor {
    private static final Set<Block> LOCKABLES = BuiltInRegistries.BLOCK_ENTITY_TYPE.stream()
            .filter(bet -> {
                var testBlock = bet.validBlocks.stream().findFirst();
                return testBlock.isPresent() && bet.create(BlockPos.ZERO, testBlock.get().defaultBlockState()) instanceof BaseContainerBlockEntity;
            }).flatMap(bet -> bet.validBlocks.stream()).collect(Collectors.toSet());

    public InventoryLockEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && LOCKABLES.contains(blockItem.getBlock());
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.TRIPWIRE_HOOK).withName("Edit Inventory Lock").withHint("Uses a named item to open").withHint("Set to ' ' to remove lock").build();
    }

    @Override
    public void open() {
        var beData = BlockItem.getBlockEntityData(stack);
        var key = beData != null && beData.contains(LockCode.TAG_LOCK, Tag.TAG_STRING) ? beData.getString(LockCode.TAG_LOCK) : "";
        Menus.string(player, key, "Editing Inventory Lock", CancellableCallback.of(newKey -> {
            if (newKey.equals(key)) {
                cancel();
            } else {
                var tag = beData == null ? new CompoundTag() : beData;
                if ("".equals(newKey)) {
                    tag.remove(LockCode.TAG_LOCK);
                } else {
                    tag.putString(LockCode.TAG_LOCK, newKey);
                }
                stack.addTagElement(BlockItem.BLOCK_ENTITY_TAG, tag);
                complete();
            }
        }, this::cancel));
    }
}
