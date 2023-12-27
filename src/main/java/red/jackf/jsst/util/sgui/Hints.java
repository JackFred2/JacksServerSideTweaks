package red.jackf.jsst.util.sgui;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface Hints {
    private static Component wrap(MutableComponent in) {
        return Component.empty().withStyle(Styles.INPUT_DECOR)
                        .append("[ ")
                        .append(in.withStyle(Styles.INPUT_KEY))
                        .append(" ]");
    }

    static Component leftClick() {
        return wrap(Component.translatable("key.mouse.left"));
    }

    static Component leftClick(Component prefix) {
        return Component.empty().withStyle(Styles.INPUT_HINT)
                        .append(prefix)
                        .append(CommonComponents.SPACE)
                        .append(leftClick());
    }

    static Component rightClick(Component prefix) {
        return Component.empty().withStyle(Styles.INPUT_HINT)
                        .append(prefix)
                        .append(CommonComponents.SPACE)
                        .append(wrap(Component.translatable("key.mouse.right")));
    }
}
