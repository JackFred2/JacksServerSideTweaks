package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;

public interface Inputs {
    static GuiElementInterface.ClickCallback leftClick(Runnable onLeftClick) {
        return (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_LEFT)  {
                onLeftClick.run();
            }
        };
    }

    static GuiElementInterface.ClickCallback leftClick(Runnable onLeftClick, GuiElementInterface.ClickCallback otherwise) {
        return (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_LEFT)  {
                onLeftClick.run();
            } else {
                otherwise.click(slot, sguiClick, mcClick, gui);
            }
        };
    }

    static GuiElementInterface.ClickCallback rightClick(Runnable onRightClick) {
        return (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_RIGHT)  {
                onRightClick.run();
            }
        };
    }

    static GuiElementInterface.ClickCallback rightClick(Runnable onRightClick, GuiElementInterface.ClickCallback otherwise) {
        return (slot, sguiClick, mcClick, gui) -> {
            if (sguiClick == ClickType.MOUSE_RIGHT)  {
                onRightClick.run();
            } else {
                otherwise.click(slot, sguiClick, mcClick, gui);
            }
        };
    }
}
