package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.WrittenBookItem;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

public class BookAuthorEditor extends Editor {

    public BookAuthorEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
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
        return Labels.create(item).withName("Edit Author").build();
    }

    @Override
    public void open() {
        var stackTag = stack.getTag();
        var author = (stackTag == null || stackTag.contains(WrittenBookItem.TAG_AUTHOR, Tag.TAG_STRING)) ? player.getGameProfile().getName() : stackTag.getString(WrittenBookItem.TAG_AUTHOR);
        Menus.string(player, author, CancellableCallback.of(newAuthor -> {
            stack.getOrCreateTag().putString(WrittenBookItem.TAG_AUTHOR, newAuthor);
            complete();
        }, this::cancel));
    }
}
