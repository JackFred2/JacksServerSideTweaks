package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

public interface CommonElements {
    static GuiElementInterface divider() {
        return GuiElementBuilder.from(Items.ORANGE_STAINED_GLASS_PANE.getDefaultInstance())
                .hideTooltip()
                .build();
    }

    static GuiElementInterface disabled() {
        return GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance())
                .hideTooltip()
                .build();
    }

    static GuiElementInterface highlight() {
        return GuiElementBuilder.from(Items.LIME_STAINED_GLASS_PANE.getDefaultInstance())
                .hideTooltip()
                .build();
    }

    static GuiElementInterface cancel(Runnable onClick) {
        return JSSTElementBuilder.from(Items.BARRIER.getDefaultInstance())
                .ui()
                .leftClick(Translations.cancel(), onClick)
                .hideDefaultTooltip()
                .build();
    }

    static GuiElementInterface close(Runnable onClick) {
        return JSSTElementBuilder.from(Items.BARRIER.getDefaultInstance())
                .ui()
                .leftClick(Translations.close(), onClick)
                .hideDefaultTooltip()
                .build();
    }

    static GuiElementInterface delete(Runnable onClick) {
        return JSSTElementBuilder.from(Items.BARRIER.getDefaultInstance())
                .ui()
                .leftClick(Translations.delete(), onClick)
                .hideDefaultTooltip()
                .build();
    }
}
