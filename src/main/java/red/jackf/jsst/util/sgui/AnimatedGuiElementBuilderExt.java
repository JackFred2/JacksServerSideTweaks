package red.jackf.jsst.util.sgui;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import net.minecraft.world.item.ItemStack;

public class AnimatedGuiElementBuilderExt extends AnimatedGuiElementBuilder {
    public AnimatedGuiElementBuilderExt addStack(ItemStack stack) {
        this.itemStacks.add(stack);
        return this;
    }
}
