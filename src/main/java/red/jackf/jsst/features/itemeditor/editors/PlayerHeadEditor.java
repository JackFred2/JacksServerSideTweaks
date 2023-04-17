package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

import static net.minecraft.world.item.ItemStack.TAG_DISPLAY_NAME;
import static net.minecraft.world.item.PlayerHeadItem.TAG_SKULL_OWNER;

public class PlayerHeadEditor extends Editor {

    public PlayerHeadEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.PLAYER_HEAD);
    }

    @Override
    public ItemStack label() {
        var item = new ItemStack(Items.PLAYER_HEAD);
        item.getOrCreateTag().putString(TAG_SKULL_OWNER, "Chihirios");
        return Labels.create(item).withName("Edit Head Owner").build();
    }

    private String getName() {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains(TAG_SKULL_OWNER, Tag.TAG_STRING)) {
                return tag.getString(TAG_SKULL_OWNER);
            } else if (tag.contains(TAG_SKULL_OWNER, Tag.TAG_COMPOUND)) {
                CompoundTag ownerTag = tag.getCompound(TAG_SKULL_OWNER);
                if (ownerTag.contains(TAG_DISPLAY_NAME, Tag.TAG_STRING)) {
                    return ownerTag.getString(TAG_DISPLAY_NAME);
                }
            }
        }
        return player.getGameProfile().getName();
    }

    @Override
    public void open() {
        Menus.string(player, getName(), newName -> {
            stack.getOrCreateTag().putString(TAG_SKULL_OWNER, newName);
            complete();
        });
    }
}
