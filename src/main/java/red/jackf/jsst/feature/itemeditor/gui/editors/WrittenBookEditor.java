package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class WrittenBookEditor extends GuiEditor {
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_AUTHOR_LENGTH = 50;

    public static final EditorType TYPE = new EditorType(
            JSST.id("written_book"),
            WrittenBookEditor::new,
            true,
            false,
            stack -> stack.is(Items.WRITTEN_BOOK),
            WrittenBookEditor::getLabel
    );

    private static GuiElementBuilderInterface<?> getLabel(EditorContext context) {
        return JSSTElementBuilder.ui(Items.WRITTEN_BOOK)
                .glow()
                .hideFlags()
                .setName(Component.translatable("jsst.itemEditor.book"));
    }

    public WrittenBookEditor(ServerPlayer player, EditorContext context, ItemStack initial, Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x1, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.book"));

        this.drawStatic();
    }

    private void drawStatic() {
        this.setSlot(1, CommonLabels.divider());

        this.setSlot(2, JSSTElementBuilder.ui(Items.ANVIL)
                .leftClick(Component.translatable("jsst.itemEditor.book.editTitle"), this::editTitle));

        this.setSlot(3, JSSTElementBuilder.ui(Items.PLAYER_HEAD)
                .setSkullOwner(player.getGameProfile(), null)
                .leftClick(Component.translatable("jsst.itemEditor.book.editAuthor"), this::editAuthor));

        if (!this.context.cosmeticOnly())
            this.setSlot(5, JSSTElementBuilder.ui(Items.WRITABLE_BOOK)
                    .addLoreLine(Component.translatable("jsst.itemEditor.book.unsign.warning").withStyle(Styles.NEGATIVE))
                    .leftClick(Component.translatable("jsst.itemEditor.book.unsign"), this::unsignBook));

        this.setSlot(8, CommonLabels.cancel(this::cancel));
    }

    @Override
    protected void redraw() {
        this.drawPreview(0);

        this.setSlot(4, JSSTElementBuilder.ui(Items.BOOK)
                .setCount(getBookGeneration() + 1)
                .leftClick(Component.translatable("jsst.itemEditor.book.editGeneration"), this::editGeneration));
    }

    private int getBookGeneration() {
        return stack.hasTag() ? WrittenBookItem.getGeneration(stack) : 0;
    }

    private void setBookTitle(String title) {
        if (title.isBlank()) {
            stack.removeTagKey(WrittenBookItem.TAG_TITLE);
        } else {
            stack.addTagElement(WrittenBookItem.TAG_TITLE, StringTag.valueOf(title));
        }
    }

    private void setBookAuthor(String author) {
        if (author.isBlank()) {
            stack.removeTagKey(WrittenBookItem.TAG_AUTHOR);
        } else {
            stack.addTagElement(WrittenBookItem.TAG_AUTHOR, StringTag.valueOf(author));
        }
    }

    private void editTitle() {
        Sounds.click(player);
        Menus.stringBuilder(player)
                .initial(stack.getItem().getName(stack).getString())
                .title(Component.translatable("jsst.itemEditor.book.editTitle"))
                .predicate(s -> s.length() <= MAX_TITLE_LENGTH)
                .createAndShow(result -> {
                    if (result.hasResult()) setBookTitle(result.result());
                    this.open();
                });
    }

    private void editAuthor() {
        Sounds.click(player);
        String initial;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(WrittenBookItem.TAG_AUTHOR, StringTag.TAG_STRING)) {
            initial = tag.getString(WrittenBookItem.TAG_AUTHOR);
        } else {
            initial = player.getGameProfile().getName();
        }
        Menus.stringBuilder(player)
                .initial(initial)
                .title(Component.translatable("jsst.itemEditor.book.editAuthor"))
                .predicate(s -> s.length() <= MAX_AUTHOR_LENGTH)
                .createAndShow(result -> {
                    if (result.hasResult()) setBookAuthor(result.result());
                    this.open();
                });
    }

    private void editGeneration() {
        Sounds.click(player);

        SelectorMenu.<Integer>builder(player)
                .title(Component.translatable("jsst.itemEditor.book.editGeneration"))
                .labelMap(LabelMaps.BOOK_GENERATIONS)
                .options(IntStream.rangeClosed(0, 3).boxed())
                .createAndShow(result -> {
                    if (result.hasResult()) stack.getOrCreateTag().putInt(WrittenBookItem.TAG_GENERATION, result.result());
                    this.open();
                });
    }

    private void unsignBook() {
        var tag = stack.getTag();
        if (tag != null) {
            var pages = tag.getList(WrittenBookItem.TAG_PAGES, Tag.TAG_STRING);
            var converted = new ListTag();
            for (Tag page : pages) {
                var component = Component.Serializer.fromJson(page.getAsString());
                if (component != null) {
                    converted.add(StringTag.valueOf(component.getString()));
                }
            }

            List.of(WrittenBookItem.TAG_TITLE,
                    WrittenBookItem.TAG_FILTERED_TITLE,
                    WrittenBookItem.TAG_PAGES,
                    WrittenBookItem.TAG_FILTERED_PAGES,
                    WrittenBookItem.TAG_AUTHOR,
                    WrittenBookItem.TAG_GENERATION,
                    WrittenBookItem.TAG_RESOLVED).forEach(tag::remove);

            tag.put(WrittenBookItem.TAG_PAGES, converted);
        }

        stack = Items.WRITABLE_BOOK.getDefaultInstance();
        stack.setTag(tag);
        complete();
    }
}
