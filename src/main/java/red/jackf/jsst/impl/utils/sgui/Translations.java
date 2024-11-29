package red.jackf.jsst.impl.utils.sgui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

import static net.minecraft.network.chat.Component.translatable;

public interface Translations {
    static MutableComponent add() {
        return translatable("jsst.ui.add");
    }

    static MutableComponent cancel() {
        return translatable("jsst.ui.cancel");
    }

    static MutableComponent change() {
        return translatable("jsst.ui.change");
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

    static MutableComponent delete() {
        return translatable("jsst.ui.delete");
    }

    static MutableComponent imprt() {
        return translatable("jsst.ui.import");
    }

    static MutableComponent export() {
        return translatable("jsst.ui.export");
    }

    static MutableComponent moveUp() {
        return translatable("jsst.ui.moveUp");
    }

    static MutableComponent moveDown() {
        return translatable("jsst.ui.moveDown");
    }

    static MutableComponent next() {
        return translatable("jsst.ui.next");
    }

    static MutableComponent open() {
        return translatable("jsst.ui.open");
    }

    static MutableComponent previous() {
        return translatable("jsst.ui.previous");
    }

    static MutableComponent randomize() {
        return translatable("jsst.ui.randomize");
    }

    static MutableComponent reset() {
        return translatable("jsst.ui.reset");
    }

    static MutableComponent save() {
        return translatable("jsst.ui.save");
    }

    static MutableComponent search() {
        return translatable("jsst.ui.search");
    }

    static MutableComponent select() {
        return translatable("jsst.ui.select");
    }

    static MutableComponent selected() {
        return translatable("jsst.ui.selected");
    }

    static MutableComponent toggle() {
        return translatable("jsst.ui.toggle");
    }

    static MutableComponent colour(DyeColor color) {
        return translatable("color.minecraft." + color.getName());
    }

    static MutableComponent split(Component first, Component second) {
        return translatable("jsst.ui.split", first, second);
    }
}
