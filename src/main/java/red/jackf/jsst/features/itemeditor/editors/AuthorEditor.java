package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.WrittenBookItem;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;

import java.util.function.Consumer;

public class AuthorEditor extends Editor {

    public AuthorEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK);
    }

    @Override
    public ItemStack label() {
        var item = new ItemStack(Items.PLAYER_HEAD);
        item.getOrCreateTag().putString(PlayerHeadItem.TAG_SKULL_OWNER, "Chihirios");
        return EditorUtils.makeLabel(item, "Edit Author");
    }

    /*private static String getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("SkullOwner", 8)) {
                return tag.getString("SkullOwner");
            } else if (tag.contains("SkullOwner", 10)) {
                CompoundTag ownerTag = tag.getCompound("SkullOwner");
                if (ownerTag.contains("Name", 8)) {
                    return ownerTag.getString("Name");
                }
            }
        }
        return "JackFred";
    }*/

    @Override
    public void open() {
        var stackTag = stack.getTag();
        var author = (stackTag == null || stackTag.contains(WrittenBookItem.TAG_AUTHOR, Tag.TAG_STRING)) ? player.getGameProfile().getName() : stackTag.getString(WrittenBookItem.TAG_AUTHOR);
        Menus.string(player, author, newAuthor -> {
            stack.getOrCreateTag().putString(WrittenBookItem.TAG_AUTHOR, newAuthor);
            complete();
        });
    }
}
