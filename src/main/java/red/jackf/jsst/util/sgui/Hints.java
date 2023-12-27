package red.jackf.jsst.util.sgui;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public interface Hints {
    private static Component wrap(Component in) {
        return Component.empty().withStyle(Styles.INPUT_HINT)
                        .append("[ ")
                        .append(in)
                        .append(" ]");
    }

    static Component leftClick() {
        return wrap(Component.translatable("key.mouse.left"));
    }

    static Component leftClick(Component prefix) {
        return Component.empty().withStyle(Styles.LABEL)
                        .append(prefix)
                        .append(CommonComponents.SPACE)
                        .append(leftClick());
    }

    static Component rightClick(Component prefix) {
        return Component.empty().withStyle(Styles.LABEL)
                        .append(prefix)
                        .append(CommonComponents.SPACE)
                        .append(wrap(Component.translatable("key.mouse.right")));
    }
}
