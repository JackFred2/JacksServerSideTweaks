package red.jackf.jsst.feature.itemeditor.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.feature.itemeditor.gui.editors.Editor;
import red.jackf.jsst.feature.itemeditor.gui.editors.SimpleNameEditor;
import red.jackf.jsst.feature.itemeditor.gui.editors.DurabilityEditor;
import red.jackf.jsst.util.sgui.*;

import java.util.List;
import java.util.OptionalInt;

public class ItemEditorGui extends SimpleGui {
    private static final List<Editor.Supplier<?>> EDITORS = List.of(
            SimpleNameEditor::new,
            DurabilityEditor::new
    );
    private final EquipmentSlot returnSlot;
    private ItemStack stack;

    public ItemEditorGui(ServerPlayer player, ItemStack initialStack, @Nullable EquipmentSlot returnSlot) {
        super(MenuType.GENERIC_9x5, player, false);
        this.stack = initialStack.copy();
        this.returnSlot = returnSlot;

        this.setTitle(Component.translatable("jsst.itemEditor.title"));

        this.drawStatic();
    }

    private void drawStatic() {
        for (int row = 0; row < 5; row++) this.setSlot(row * 9 + 3, CommonLabels.divider());

        this.setSlot(Slots.CANCEL, CommonLabels.cancel(this::close));
    }

    @Override
    public void onOpen() {
        // update result stack
        this.setSlot(Slots.RESULT, GuiElementBuilder.from(stack.copy())
                                                    .setName(Util.getLabelAsTooltip(stack))
                                                    .addLoreLine(Hints.leftClick(Translations.save()))
                                                    .setCallback(Inputs.leftClick(this::complete)));

        // update editors
        Util.fill(this, ItemStack.EMPTY, 4, 9, 0, 5);
        final Util.SlotTranslator slotTranslator = Util.slotTranslator(4, 9, 0, 5);

        int index = 0;
        for (Editor.Supplier<?> editorConstructor : EDITORS) {
            OptionalInt slot = slotTranslator.translate(index);
            if (slot.isEmpty()) break;
            Editor built = editorConstructor.create(player, this.stack, this::onResult);
            if (built.appliesTo(this.stack)) {
                this.setSlot(slot.getAsInt(), built.getLabel()
                                                   .addLoreLine(Hints.leftClick(Translations.open()))
                                                   .setCallback(Inputs.leftClick(built::start)));
                index++;
            }
        }
    }

    private void onResult(ItemStack result) {
        this.stack = result;
        this.open();
    }

    private void complete() {
        Sounds.click(player);
        if (returnSlot != null) {
            this.player.setItemSlot(returnSlot, this.stack);
        } else {
            this.player.getInventory().placeItemBackInInventory(this.stack);
        }
        this.close();
    }

    private static class Slots {
        private static final int RESULT = 10;
        private static final int CANCEL = 36;
    }
}
