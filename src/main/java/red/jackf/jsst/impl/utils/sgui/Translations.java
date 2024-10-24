package red.jackf.jsst.impl.utils.sgui;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

import static net.minecraft.network.chat.Component.translatable;

public interface Translations {
    static MutableComponent cancel() {
        return translatable("jsst.ui.cancel");
    }

    static MutableComponent clear() {
        return translatable("jsst.ui.clear");
    }

    static MutableComponent close() {
        return translatable("jsst.ui.close");
    }

    static MutableComponent complete() {
        return translatable("jsst.ui.complete");
    }

    static MutableComponent confirm() {
        return translatable("jsst.ui.confirm");
    }

    static MutableComponent open() {
        return translatable("jsst.ui.open");
    }

    static MutableComponent reset() {
        return translatable("jsst.ui.reset");
    }

    static MutableComponent save() {
        return translatable("jsst.ui.save");
    }

    static MutableComponent select() {
        return translatable("jsst.ui.select");
    }

    static MutableComponent toggle() {
        return translatable("jsst.ui.toggle");
    }

    static MutableComponent colour(DyeColor color) {
        return translatable("color.minecraft." + color.getName());
    }

}
