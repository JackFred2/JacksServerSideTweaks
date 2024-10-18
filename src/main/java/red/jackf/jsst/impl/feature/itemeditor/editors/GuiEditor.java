package red.jackf.jsst.impl.feature.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;

public abstract class GuiEditor extends SimpleGuiExt {
    protected final EditSession session;

    public GuiEditor(Component title, MenuType<?> type, EditSession session, boolean manipulatePlayerSlots) {
        super(type, session.getPlayer(), manipulatePlayerSlots);
        this.setTitle(title);
        this.session = session;
    }

    @Override
    @MustBeInvokedByOverriders
    public void beforeOpen() {
        super.beforeOpen();
        this.drawStatic();
        this.refresh();
    }

    /**
     * Add unchanging content here.
     */
    protected void drawStatic() {}

    /**
     * Add content that changes (lists of elements, items, etc)
     */
    protected void refresh() {}

    // previews

    /**
     * Draws the current stack at a given column and row
     */
    protected void drawPreview(int column, int row) {
        this.setSlot(column, row, this.session.getStack().copy());
    }

    /**
     * Draws the current stack at a given slot
     */
    protected void drawPreview(int slot) {
        this.setSlot(slot, this.session.getStack().copy());
    }
}
