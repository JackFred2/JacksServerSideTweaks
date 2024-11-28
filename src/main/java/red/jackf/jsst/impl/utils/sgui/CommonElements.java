package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

public interface CommonElements {
    static GuiElementInterface divider(@NotNull DyeColor colour) {
        return GuiElementBuilder.from(ColourUtils.STAINED_GLASS.get(colour).getDefaultInstance())
                .hideTooltip()
                .build();
    }

    static GuiElementInterface divider() {
        return divider(DyeColor.ORANGE);
    }

    static GuiElementInterface disabled(Component... lines) {
        if (lines.length == 0)
            return JSSTElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE)
                    .hideTooltip()
                    .build();
        else {
            var builder = JSSTElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE).ui()
                    .hideDefaultTooltip();

            for (Component line : lines) {
                builder.addLoreLine(line);
            }

            return builder.build();
        }
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

    static GuiElementInterface clear(Runnable onClick) {
        return JSSTElementBuilder.from(Items.GRINDSTONE.getDefaultInstance())
                .ui()
                .leftClick(Translations.clear(), onClick)
                .hideDefaultTooltip()
                .build();
    }
}
