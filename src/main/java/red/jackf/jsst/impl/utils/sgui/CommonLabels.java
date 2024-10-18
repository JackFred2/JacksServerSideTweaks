package red.jackf.jsst.impl.utils.sgui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface CommonLabels {
    static ItemStack divider() {
        return GuiElementBuilder.from(Items.ORANGE_STAINED_GLASS_PANE.getDefaultInstance())
                .hideTooltip()
                .asStack();
    }


}
