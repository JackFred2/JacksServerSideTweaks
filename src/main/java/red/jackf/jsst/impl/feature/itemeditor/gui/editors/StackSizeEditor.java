package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;

import java.util.List;
import java.util.function.Consumer;

public class StackSizeEditor extends GuiEditor {
    public static Type<StackSizeEditor> TYPE = Editor.<StackSizeEditor>typeBuilder(JSST.id("stack_size"))
            .factory(StackSizeEditor::new)
            .labelFactory(StackSizeEditor::getIcon)
            .build();

    private static GuiElementInterface getIcon(EditSession session) {
        AnimatedGuiElementBuilderExt builder = new AnimatedGuiElementBuilderExt();

        builder.setInterval(4);

        for (int i : List.of(1, 2, 4, 8, 16, 32, 64, 99)) {
            builder.addStack(JSSTElementBuilder.from(session.getStack())
                    .hideDefaultTooltip()
                    .removeComponent(DataComponents.LORE)
                    .setName(Component.translatable("jsst.itemEditor.editor.stackSize"))
                    .setCount(i)
                    .asStack());
        }

        return builder.build();
    }

    public StackSizeEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.stackSize"), MenuType.GENERIC_9x1, false);

        this.refreshTitle();
    }

    private void refreshTitle() {
        this.setTitle(Translations.split(Component.translatable("jsst.itemEditor.editor.stackSize"), Component.translatable("jsst.itemEditor.editor.stackSize.currentMax", this.stack.getMaxStackSize())));
    }

    @Override
    protected void drawStatic() {
        this.setSlot(1, CommonElements.divider());

        this.setSlot(2, JSSTElementBuilder.from(Items.BOOK).ui().glow()
                .leftClick(Component.translatable("jsst.itemEditor.editor.stackSize.setCount"), this::openSetCount));

        int defaultMax = this.stack.getItem().getDefaultMaxStackSize();

        this.setSlot(4, CommonElements.divider());

        this.setSlot(5, JSSTElementBuilder.from(Items.BOOKSHELF).ui().glow()
                .leftClick(Component.translatable("jsst.itemEditor.editor.stackSize.setMaxCount"), this::openSetMaxCount));

        var quickSetMaxCountBuilder = JSSTElementBuilder.from(Items.BOOKSHELF.getDefaultInstance()).ui()
                .setCount(defaultMax)
                .leftClick(Component.translatable("jsst.itemEditor.editor.stackSize.setMaxCountTo", defaultMax), () -> {
                    Sounds.UI.click(player);
                    this.setMaxCount(defaultMax);
                    this.refresh();
                });
        if (defaultMax != 1)
            quickSetMaxCountBuilder.rightClick(Component.translatable("jsst.itemEditor.editor.stackSize.setMaxCountTo", 1), () -> {
                Sounds.UI.click(player);
                this.setMaxCount(1);
                this.refresh();
            });
        this.setSlot(6, quickSetMaxCountBuilder);

        this.setSlot(7, CommonElements.divider());

        this.setSlot(8, CommonElements.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        this.drawPreview(0);

        int max = this.stack.getMaxStackSize();

        var quickSetCountBuilder = JSSTElementBuilder.from(Items.BOOK.getDefaultInstance()).ui()
                .setCount(max)
                .leftClick(Component.translatable("jsst.itemEditor.editor.stackSize.setCountTo", max), () -> {
                    Sounds.UI.click(player);
                    this.setCount(max);
                    this.refresh();
                });
        if (max != 1)
            quickSetCountBuilder.rightClick(Component.translatable("jsst.itemEditor.editor.stackSize.setCountTo", 1), () -> {
                Sounds.UI.click(player);
                this.setCount(1);
                this.refresh();
            });
        this.setSlot(3, quickSetCountBuilder);
    }

    private void openSetMaxCount() {
        Sounds.UI.click(player);

        InputMenus.integer(player, 1, 99)
                .title(Component.translatable("jsst.itemEditor.editor.stackSize.setMaxCount"))
                .start(opt -> {
                    opt.ifPresent(this::setMaxCount);

                    this.open();
                });
    }

    private void openSetCount() {
        Sounds.UI.click(player);

        InputMenus.integer(player, 1, 99)
                .title(Component.translatable("jsst.itemEditor.editor.stackSize.setCount"))
                .start(opt -> {
                    opt.ifPresent(this::setCount);

                    this.open();
                });
    }

    private void setCount(int count) {
        if (count > this.stack.getMaxStackSize()) {
            this.setMaxCount(count);
        }

        this.stack.setCount(count);
    }

    private void setMaxCount(int count) {
        this.stack.set(DataComponents.MAX_STACK_SIZE, count);
        this.stack.limitSize(count);

        this.refreshTitle();
    }
}
