package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

public interface CommonLabels {
    static ItemStack divider() {
        return GuiElementBuilder.from(Items.ORANGE_STAINED_GLASS_PANE.getDefaultInstance())
                .hideTooltip()
                .asStack();
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
}
