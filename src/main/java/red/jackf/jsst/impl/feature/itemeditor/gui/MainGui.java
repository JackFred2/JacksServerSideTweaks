package red.jackf.jsst.impl.feature.itemeditor.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.ItemEditor;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.feature.itemeditor.gui.editors.Editor;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.*;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.WrappedElement;
import java.util.List;
import java.util.function.Consumer;

public class MainGui extends SimpleGuiExt {
    private final EditSession session;
    private final Consumer<Result> resultConsumer;

    public MainGui(EditSession session, Consumer<Result> resultConsumer) {
        super(MenuType.GENERIC_9x6, session.getPlayer(), false);
        this.session = session;
        this.resultConsumer = resultConsumer;
        this.setTitle(Component.translatable("jsst.itemEditor"));

        this.drawStatic();
    }

    @Override
    protected void drawStatic() {
        UIRegion.column(this, 3).fillStack(CommonLabels::divider);

        this.setSlot(0, 5, CommonLabels.close(() -> {
            Sounds.UI.close(player);
            this.session.end();
        }));
    }

    @Override
    protected void refresh() {
        this.setSlot(1, 1, JSSTElementBuilder.from(this.session.getStack())
                .leftClick(Translations.complete(), this::complete));

        List<WrappedElement<GuiElementInterface>> buttons = ItemEditor.EDITORS.stream()
                .filter(type -> type.appliesTo(this.session))
                .map(type -> WrappedElement.builder(type.getIcon(this.session))
                        .leftClick(Translations.open(), () -> {
                            Editor editor = type.create(this.session, this::onResult);
                            editor.start();
                        }).build())
                .toList();

        UIRegion.rectangle(this, 4, 0, 9, this.getHeight())
                .loadElements(buttons);
    }

    private void complete() {
        Sounds.Ding.success(player);
        this.resultConsumer.accept(Result.of(this.session.getStack()));
        this.session.end();
    }

    private void onResult(Result result) {
        if (result.hasResult()) this.session.setStack(result.result());
        this.open();
    }
}
