package red.jackf.jsst.impl.utils.sgui;

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

    static Component leftClick(Component label) {
        return Component.empty().withStyle(Styles.INPUT_HINT)
                .append(label)
                .append(CommonComponents.space())
                .append(wrap(Component.translatable("key.mouse.left")));
    }

    static Component rightClick(Component label) {
        return Component.empty().withStyle(Styles.INPUT_HINT)
                .append(label)
                .append(CommonComponents.space())
                .append(wrap(Component.translatable("key.mouse.right")));
    }
}
