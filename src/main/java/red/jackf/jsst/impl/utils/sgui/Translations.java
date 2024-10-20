package red.jackf.jsst.impl.utils.sgui;

import net.minecraft.network.chat.MutableComponent;

import static net.minecraft.network.chat.Component.translatable;

public interface Translations {
    static MutableComponent open() {
        return translatable("jsst.ui.open");
    }

    static MutableComponent complete() {
        return translatable("jsst.ui.complete");
    }

    static MutableComponent save() {
        return translatable("jsst.ui.save");
    }

    static MutableComponent reset() {
        return translatable("jsst.ui.reset");
    }

    static MutableComponent close() {
        return translatable("jsst.ui.close");
    }

    static MutableComponent cancel() {
        return translatable("jsst.ui.cancel");
    }
}
