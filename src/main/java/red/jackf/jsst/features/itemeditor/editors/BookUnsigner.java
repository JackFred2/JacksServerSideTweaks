package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

public class BookUnsigner extends Editor {
    public BookUnsigner(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.WRITABLE_BOOK).withName("Unsign Book").build();
    }

    @Override
    public void playOpenSound() {
        // noop
    }

    @Override
    public void open() {
        var newStack = new ItemStack(Items.WRITABLE_BOOK);
        var pagesTag = stack.getOrCreateTag().getList(WrittenBookItem.TAG_PAGES, Tag.TAG_STRING);
        var newPages = new ListTag();
        for (Tag page : pagesTag) {
            var component = Component.Serializer.fromJson(page.getAsString());
            if (component != null) newPages.add(StringTag.valueOf(component.getString()));
        }
        newStack.addTagElement(WrittenBookItem.TAG_PAGES, newPages);
        stack = newStack;
        complete();
    }
}
