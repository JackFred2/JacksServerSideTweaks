package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;

public interface Inputs {
    static GuiElementInterface.ClickCallback leftClick(Runnable onLeft) {
        return (slot, clickType, slotAction, gui) -> {
            if (clickType == ClickType.MOUSE_LEFT) onLeft.run();
        };
    }
}
