package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonLabels;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

import java.util.function.Consumer;

public class SimpleNameEditor extends GuiEditor {
    public static final Type<SimpleNameEditor> TYPE = new Type<>(
            JSST.id("simple_name"),
            SimpleNameEditor::new,
            session -> true,
            session -> JSSTElementBuilder.from(Items.NAME_TAG.getDefaultInstance())
                    .setName(Component.translatable("jsst.itemEditor.editor.simpleName"))
                    .hideDefaultTooltip()
                    .build()
    );

    public SimpleNameEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.simpleName"), MenuType.GENERIC_9x1, false);
    }

    @Override
    protected void drawStatic() {
        this.setSlot(8, CommonLabels.cancel(this::cancel));

        this.setSlot(1, CommonLabels.divider());

        this.setSlot(2, JSSTElementBuilder.from(Items.PAPER).ui()
                .leftClick(Component.translatable("jsst.itemEditor.changeText"), this::changeText));
    }

    @Override
    protected void refresh() {
        this.drawPreview(0);

        if (this.stack.has(DataComponents.CUSTOM_NAME)) {
            this.setSlot(7, JSSTElementBuilder.from(Items.GRINDSTONE).ui()
                    .leftClick(Translations.clear(), this::clearName));
        } else {
            this.clearSlot(7);
        }
    }

    private void clearName() {
        Sounds.grind(player);
        this.stack.remove(DataComponents.CUSTOM_NAME);
        this.refresh();
    }

    private void changeText() {
        Sounds.click(player);
    }
}
