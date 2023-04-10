package red.jackf.jsst.features.itemeditor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.features.Util;
import red.jackf.jsst.features.itemeditor.editors.AdvancedNameEditor;
import red.jackf.jsst.features.itemeditor.editors.Editor;

import java.util.HashMap;
import java.util.List;

import static net.minecraft.network.chat.Component.literal;

public class EditSession {
    private static final List<Editor.Supplier> EDITORS = List.of(
            AdvancedNameEditor::new
    );

    private final ServerPlayer player;
    private ItemStack stack;

    public EditSession(ServerPlayer player, ItemStack stack) {
        this.player = player;
        this.stack = stack;
    }

    public void mainMenu() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.withHint(stack, "Click to finish"), this::finish));

        for (var slot : new int[]{3, 12, 21}) { // divider
            elements.put(slot, new ItemGuiElement(EditorUtils.DIVIDER.copy(), null));
        }

        var editors = EDITORS.stream().map(b -> b.get(stack.copy(), player, stack -> {
            this.stack = stack;
            mainMenu();
        })).filter(e -> e.applies(stack)).toList();

        for (int i = 0; i < editors.size(); i++) { // 3x5 area on the right side
            var row = i / 5;
            var column = 4 + (i % 5);
            var editor = editors.get(i);
            elements.put(row * 9 + column, new ItemGuiElement(editor.label(), editor::open));
        }

        player.openMenu(EditorUtils.make9x3(literal("Item Editor"), elements));
    }

    private void finish() {
        Util.successSound(player);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.closeContainer();
    }

    public void start() {
        mainMenu();
    }
}
