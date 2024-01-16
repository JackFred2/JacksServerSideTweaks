package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;

public interface CommonLabels {
    static GuiElementInterface close(Runnable closeCallback) {
        return JSSTElementBuilder.ui(Items.BARRIER)
                .leftClick(Translations.close(), closeCallback)
                .build();
    }

    static GuiElementInterface cancel(Runnable cancelCallback) {
        return JSSTElementBuilder.ui(Items.BARRIER)
                .leftClick(Translations.cancel(), cancelCallback)
                .build();
    }

    static GuiElementInterface divider() {
        return JSSTElementBuilder.ui(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .setName(CommonComponents.EMPTY)
                .build();
    }

    static GuiElementInterface disabled() {
        return disabled(Component.empty());
    }

    static GuiElementInterface disabled(Component text) {
        return JSSTElementBuilder.ui(Items.GRAY_STAINED_GLASS_PANE)
                .setName(text)
                .build();
    }
}
