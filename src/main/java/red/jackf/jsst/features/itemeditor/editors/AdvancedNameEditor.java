package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.menus.AdvancedComponentMenu;

import java.util.function.Consumer;

public class AdvancedNameEditor extends Editor {
    public AdvancedNameEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(new ItemStack(Items.ANVIL), "Edit Name (Advanced)");
    }

    @Override
    public void open() {
        new AdvancedComponentMenu(player, stack, stack.hasCustomHoverName() ? stack.getHoverName() : null, AdvancedComponentMenu.BlankBehaviour.SHOW_STACK_NAME, c -> {
            stack.setHoverName(c);
            complete();
        }).open();
    }
}
