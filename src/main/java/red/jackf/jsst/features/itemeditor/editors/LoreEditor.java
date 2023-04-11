package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;

import java.util.function.Consumer;

public class LoreEditor extends Editor {
    public LoreEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(Items.WRITABLE_BOOK, "Edit Lore");
    }

    @Override
    public void open() {

    }
}
