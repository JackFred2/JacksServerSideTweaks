package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.function.Consumer;

public class BookUnsigner implements Editor {
    public static final Type<BookUnsigner> TYPE = Editor.<BookUnsigner>typeBuilder(JSST.id("unsign_book"))
            .factory(BookUnsigner::new)
            .labelFactory(BookUnsigner::getLabel)
            .inputHint(Translations::select)
            .appliesTo(session -> session.getStack().has(DataComponents.WRITTEN_BOOK_CONTENT))
            .supportsCosmetic()
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.FEATHER).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.unsignBook"))
                .addLoreLine(Component.translatable("jsst.itemEditor.editor.unsignBook.warning").withStyle(Styles.NEGATIVE))
                .build();
    }

    private final EditSession session;
    private final Consumer<Result> callback;

    public BookUnsigner(EditSession session, Consumer<Result> callback) {
        this.session = session;
        this.callback = callback;
    }

    @Override
    public void start() {
        Sounds.playSound(this.session.getPlayer(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1.1f);

        ItemStack asWritableBook = this.session.getStack().transmuteCopy(Items.WRITABLE_BOOK);
        WrittenBookContent written = asWritableBook.remove(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null) {
            WritableBookContent writable = new WritableBookContent(written.pages().stream()
                    .map(f -> Filterable.passThrough(f.raw().getString()))
                    .toList());
            asWritableBook.set(DataComponents.WRITABLE_BOOK_CONTENT, writable);
        }

        callback.accept(Result.of(asWritableBook));
    }
}
