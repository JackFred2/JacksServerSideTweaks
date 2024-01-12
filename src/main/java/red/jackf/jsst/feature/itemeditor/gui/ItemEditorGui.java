package red.jackf.jsst.feature.itemeditor.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.feature.itemeditor.gui.editors.*;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.WrappedElement;

import java.util.List;

public class ItemEditorGui extends SimpleGui {
    private static final List<Editor.EditorType> EDITORS = List.of(
            SimpleNameEditor.TYPE,
            NameEditor.TYPE,
            LoreEditor.TYPE,
            DurabilityEditor.TYPE,
            TrimEditor.TYPE,
            PotionEditor.TYPE,
            StackNBTPrinter.TYPE
    );
    private final EquipmentSlot returnSlot;
    private final EditorContext context;
    private ItemStack stack;

    public ItemEditorGui(
            ServerPlayer player,
            ItemStack initialStack,
            @Nullable EquipmentSlot returnSlot,
            EditorContext context) {
        super(MenuType.GENERIC_9x5, player, false);
        this.stack = initialStack.copy();
        this.returnSlot = returnSlot;
        this.context = context;

        this.setTitle(Component.translatable("jsst.itemEditor.title"));

        this.drawStatic();
    }

    private void drawStatic() {
        for (int row = 0; row < 5; row++) this.setSlot(row * 9 + 3, CommonLabels.divider());

        this.setSlot(Util.slot(0, 4), CommonLabels.cancel(this::close));
    }

    @Override
    public void onOpen() {
        // update result stack
        this.setSlot(Util.slot(1, 1), GuiElementBuilder.from(stack.copy())
                                                       .setName(Util.getLabelAsTooltip(stack))
                                                       .addLoreLine(Hints.leftClick(Translations.save()))
                                                       .setCallback(Inputs.leftClick(this::complete)));

        final var editors = EDITORS.stream()
                                   .filter(type -> type.appliesTo().test(stack))
                                   .filter(type -> !context.cosmeticOnly() || type.cosmeticOnly())
                                   .map(editorType -> new WrappedElement(editorType.labelSupplier().apply(context).build(),
                                                                         List.of(Hints.leftClick(Translations.open())),
                                                                         (slot, guiClickType, rawClickType, gui) -> {
                                                                             if (guiClickType == ClickType.MOUSE_LEFT)
                                                                                 editorType.constructor()
                                                                                           .create(this.player, this.context, this.stack, this::onResult)
                                                                                           .run();
                                                                         }))
                                   .toList();

        // update editors
        final GridTranslator gridTranslator = GridTranslator.between(4, 9, 0, 5);
        gridTranslator.fill(this, ItemStack.EMPTY);

        for (var pair : gridTranslator.iterate(editors)) {
            this.setSlot(pair.slot(), pair.item());
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
}
