package red.jackf.jsst.util.sgui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public interface Hints {
    Style STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);

    private static Component wrap(Component in) {
        return Component.empty().withStyle(STYLE).append("[ ").append(in).append(" ]");
    }

    static Component leftClick() {
        return wrap(Component.translatable("key.mouse.left"));
    }

    static Component leftClick(Component prefix) {
        return Component.empty().withStyle(Styles.LABEL).append(prefix).append(CommonComponents.SPACE).append(leftClick());
    }
}
