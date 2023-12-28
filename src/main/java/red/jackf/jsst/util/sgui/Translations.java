package red.jackf.jsst.util.sgui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

public interface Translations {
    static MutableComponent cancel() {
        return Component.translatable("gui.cancel");
    }

    static MutableComponent close() {
        return Component.translatable("mco.selectServer.close");
    }

    static MutableComponent clear() {
        return Component.translatable("jsst.common.clear");
    }

    static MutableComponent change() {
        return Component.translatable("jsst.common.change");
    }

    static MutableComponent confirm() {
        return Component.translatable("jsst.common.confirm");
    }

    static MutableComponent search() {
        return Component.translatable("gui.recipebook.search_hint");
    }

    static MutableComponent reset() {
        return Component.translatable("controls.reset");
    }

    static MutableComponent open() {
        return Component.translatable("jsst.common.open");
    }

    static MutableComponent save() {
        return Component.translatable("jsst.common.save");
    }

    static MutableComponent dye(DyeColor colour) {
        return Component.translatable("color.minecraft." + colour.getName());
    }

    static MutableComponent select() {
        return Component.translatable("mco.template.button.select");
    }

    static MutableComponent toggle() {
        return Component.translatable("jsst.common.toggle");
    }

    static MutableComponent current(Object object) {
        return Component.translatable("mco.configure.world.minigame", object);
    }

    static MutableComponent def() {
        return Component.translatable("resourcePack.vanilla.name");
    }
}
