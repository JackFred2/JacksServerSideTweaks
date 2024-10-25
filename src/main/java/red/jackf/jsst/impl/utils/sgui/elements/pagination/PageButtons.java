package red.jackf.jsst.impl.utils.sgui.elements.pagination;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

import java.util.function.Consumer;

public record PageButtons(int previousButton, int currentPage, int nextButton) {
    void draw(SimpleGuiExt gui, int page, int maxPage, Consumer<Integer> pageChangeCallback) {
        boolean firstPage = page <= 0;
        boolean lastPage = page >= maxPage;

        if (firstPage) {
            gui.clearSlot(previousButton);
        } else {
            gui.setSlot(previousButton, JSSTElementBuilder.from(Items.RED_CONCRETE).ui()
                    .leftClick(Translations.previous(), () -> {
                        int newPage = page - 1;
                        Sounds.UI.progress(gui.getPlayer(), newPage);
                        pageChangeCallback.accept(newPage);
                    }));
        }

        gui.setSlot(currentPage, JSSTElementBuilder.from(Items.PAPER).ui()
                .setName(Component.translatable("book.pageIndicator", page + 1, maxPage + 1))
                .setCount(page + 1));

        if (lastPage) {
            gui.clearSlot(nextButton);
        } else {
            gui.setSlot(nextButton, JSSTElementBuilder.from(Items.LIME_CONCRETE).ui()
                    .leftClick(Translations.next(), () -> {
                        int newPage = page + 1;
                        Sounds.UI.progress(gui.getPlayer(), newPage);
                        pageChangeCallback.accept(newPage);
                    }));
        }
    }
}
