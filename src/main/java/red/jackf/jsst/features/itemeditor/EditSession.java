package red.jackf.jsst.features.itemeditor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.editors.AdvancedNameEditor;
import red.jackf.jsst.features.itemeditor.editors.Editor;
import red.jackf.jsst.features.itemeditor.editors.SimpleNameEditor;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;

import java.util.HashMap;
import java.util.List;

import static net.minecraft.network.chat.Component.literal;

public class EditSession {
    private static final List<Editor.Supplier> EDITORS = List.of(
            SimpleNameEditor::new,
            AdvancedNameEditor::new
    );

    private final ServerPlayer player;
    private ItemStack stack;
    @Nullable
    private final EquipmentSlot toReplace;

    public EditSession(ServerPlayer player, ItemStack stack, @Nullable EquipmentSlot toReplace) {
        this.player = player;
        this.stack = stack;
        this.toReplace = toReplace;
    }

    public void mainMenu() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.withHint(stack, "Click to finish"), this::finish));

        for (var slot : new int[]{3, 12, 21}) { // divider
            elements.put(slot, EditorUtils.divider());
        }

        var editors = EDITORS.stream().map(b -> b.get(stack.copy(), player, stack -> {
            this.stack = stack;
            mainMenu();
        })).filter(e -> e.applies(stack)).toList();

        for (int i = 0; i < editors.size(); i++) { // 3x5 area on the right side
            var row = i / 5;
            var column = 4 + (i % 5);
            var editor = editors.get(i);
            elements.put(row * 9 + column, new ItemGuiElement(editor.label(), () -> {
                Sounds.interact(player);
                editor.open();
            }));
        }

        player.openMenu(EditorUtils.make9x3(literal("Item Editor"), elements));
    }

    private void finish() {
        Sounds.complete(player);
        if (toReplace != null) {
            player.setItemSlot(toReplace, stack); // replace held item
        } else {
            if (!player.getInventory().add(stack)) player.drop(stack, false); // give or drop if full
        }
        player.closeContainer();
    }

    public void start() {
        mainMenu();
    }
}
