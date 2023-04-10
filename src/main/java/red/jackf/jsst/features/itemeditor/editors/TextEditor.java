package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.EditorUtils;
import red.jackf.jsst.features.itemeditor.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.JSSTSealableMenuWithButtons;

import java.util.HashMap;
import java.util.function.Consumer;

public class TextEditor {

    public static void create(ServerPlayer player, String text, Consumer<String> callback) {
        player.openMenu(new SimpleMenuProvider(((i, inventory, player1) -> {
            var menu = new AnvilMenu(i, inventory);
            var elements = new HashMap<Integer, ItemGuiElement>();
            elements.put(AnvilMenu.INPUT_SLOT, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, text, "Click to cancel"), () -> callback.accept(text)));
            elements.put(AnvilMenu.RESULT_SLOT, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, text, "Click to confirm"), () -> {
                var item = menu.slots.get(AnvilMenu.RESULT_SLOT).getItem();
                if (item.isEmpty() || !item.hasCustomHoverName())
                    callback.accept(text);
                else
                    callback.accept(item.getHoverName().getString());
            }));
            elements.forEach((slot, label) -> menu.slots.get(slot).set(label.label()));
            //noinspection DataFlowIssue
            ((JSSTSealableMenuWithButtons) menu).jsst_sealWithButtons(elements);
            return menu;
        }), Component.literal("Editing text")));
    }
}
