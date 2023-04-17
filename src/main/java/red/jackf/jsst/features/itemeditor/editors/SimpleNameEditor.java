package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.HashMap;
import java.util.function.Consumer;

public class SimpleNameEditor extends Editor {
    public SimpleNameEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.NAME_TAG).withName("Edit Name (Simple)").build();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(0, new ItemGuiElement(EditorUtils.withHint(stack, "Click to finish"), this::complete));
        elements.put(1, EditorUtils.divider());
        elements.put(2, new ItemGuiElement(Labels.create(Items.PAPER).withName("Edit Name").build(), () -> {
            Sounds.interact(player);
            Menus.string(player, stack.getHoverName().getString(), CancellableCallback.of(newStr -> {
                Sounds.success(player);
                stack.setHoverName(Component.literal(newStr).setStyle(stack.getHoverName().getStyle()));
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(3, new ItemGuiElement(Labels.create(EditorUtils.colourToItem(stack.getHoverName().getStyle().getColor())).withName("Edit Style").build(), () -> {
            Sounds.interact(player);
            Menus.style(player, stack.getHoverName(), c -> {
                stack.setHoverName(c);
                open();
            });
        }));

        elements.put(6, EditorUtils.clear(() -> {
            Sounds.clear(player);
            stack.resetHoverName();
            open();
        }));
        elements.put(7, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            open();
        }));
        elements.put(8, EditorUtils.cancel(this::cancel));

        player.openMenu(EditorUtils.make9x1(Component.literal("Editing Name"), elements));
    }
}
