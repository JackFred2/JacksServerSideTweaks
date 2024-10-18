package red.jackf.jsst.impl.feature.itemeditor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.impl.feature.itemeditor.editors.GuiEditor;
import red.jackf.jsst.impl.utils.sgui.CommonLabels;
import red.jackf.jsst.impl.utils.sgui.UIRegion;

public class MainGui extends GuiEditor {
    public MainGui(EditSession session) {
        super(Component.translatable("jsst.itemEditor"), MenuType.GENERIC_9x6, session, false);
    }

    @Override
    protected void drawStatic() {
        UIRegion.column(this, 3).fillStack(CommonLabels::divider);
    }

    @Override
    protected void refresh() {
        this.drawPreview(1, 1);
    }
}
