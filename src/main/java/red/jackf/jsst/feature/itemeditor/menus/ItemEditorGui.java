package red.jackf.jsst.feature.itemeditor.menus;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.sgui.*;

public class ItemEditorGui extends SimpleGui {
    private static final int RESULT = 10;
    private final EquipmentSlot returnSlot;

    private ItemStack stack;

    public ItemEditorGui(ServerPlayer player, ItemStack initialStack, @Nullable EquipmentSlot returnSlot) {
        super(MenuType.GENERIC_9x5, player, false);
        this.stack = initialStack;
        this.returnSlot = returnSlot;

        this.setTitle(Component.translatable("jsst.itemEditor.title"));

        this.drawStatic();
    }

    private void drawStatic() {
        for (int row = 0; row < 5; row++) this.setSlot(row * 9 + 3, CommonLabels.divider());

        this.setSlot(36, CommonLabels.cancel(this::close));
    }

    @Override
    public void onOpen() {
        // update result stack
        this.setSlot(RESULT, GuiElementBuilder.from(stack)
                                              .addLoreLine(Hints.leftClick(Component.translatable("jsst.common.save")))
                                              .setCallback(Inputs.leftClick(this::complete)));

        // update editors
        Util.fill(this, ItemStack.EMPTY, 4, 9, 0, 5);
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
