package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Util;
import red.jackf.jsst.features.itemeditor.EditorUtils;
import red.jackf.jsst.features.itemeditor.ItemGuiElement;

import java.util.HashMap;
import java.util.function.Consumer;

public class SimpleNameEditor extends Editor {
    public SimpleNameEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(Items.NAME_TAG, "Edit Name (Simple)");
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(0, new ItemGuiElement(EditorUtils.withHint(stack, "Click to finish"), this::complete));
        elements.put(1, EditorUtils.divider());
        elements.put(2, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, "Edit Name"), () -> EditorUtils.textEditor(player, stack.getHoverName().getString(), newStr -> {
            Util.successSound(player);
            stack.setHoverName(Component.literal(newStr).setStyle(stack.getHoverName().getStyle()));
            open();
        })));
        elements.put(3, new ItemGuiElement(EditorUtils.makeLabel(EditorUtils.colourToItem(stack.getHoverName().getStyle().getColor()), "Edit Style"), () -> StyleEditor.create(player, stack.getHoverName(), c -> {
            stack.setHoverName(c);
            open();
        })));
        elements.put(4, new ItemGuiElement(EditorUtils.makeLabel(Items.WATER_BUCKET, "Clear Custom Name"), () -> {  // TODO: Add confirmation dialogue when modified
            stack.resetHoverName();
            open();
        }));

        elements.put(8, EditorUtils.cancel(this::cancel));

        player.openMenu(EditorUtils.make9x1(Component.literal("Editing name"), elements));
    }
}
