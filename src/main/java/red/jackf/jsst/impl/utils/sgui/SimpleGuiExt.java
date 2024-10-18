package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class SimpleGuiExt extends SimpleGui {
    public SimpleGuiExt(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);
    }

    // helper methods for using column and row

    public int getSlotFor(int column, int row) {
        int slot = this.getWidth() * row + column;
        if (slot < 0 || slot >= this.getSize()) throw new IllegalArgumentException("Slot (%d, %d) = %d out of range [0-%d]".formatted(column, row, slot, this.size - 1));
        return slot;
    }

    public void setSlot(int column, int row, GuiElementInterface guiElementInterface) {
        this.setSlot(getSlotFor(column, row), guiElementInterface);
    }

    public void setSlot(int column, int row, GuiElementBuilderInterface<?> element) {
        this.setSlot(getSlotFor(column, row), element);
    }

    public void setSlot(int column, int row, ItemStack itemStack) {
        this.setSlot(getSlotFor(column, row), itemStack);
    }

    public void setSlotRedirect(int column, int row, Slot slot) {
        this.setSlotRedirect(getSlotFor(column, row), slot);
    }
}
