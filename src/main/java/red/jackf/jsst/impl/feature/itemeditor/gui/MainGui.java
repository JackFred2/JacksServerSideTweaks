package red.jackf.jsst.impl.feature.itemeditor.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.ItemEditor;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.feature.itemeditor.gui.editors.Editor;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.*;
import red.jackf.jsst.impl.utils.sgui.elements.ToggleButton;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.WrappedElement;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

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
        UIRegion.column(this, 3).fillElement(CommonElements::divider);

        this.setSlot(0, 5, CommonElements.close(() -> {
            Sounds.UI.close(player);
            this.session.end();
        }));

        this.setSlot(0, 4, ToggleButton.builder(Component.translatable("jsst.itemEditor.showDeveloper"))
                .initial(this.session.isShowingDeveloperTools())
                .enabled(JSSTElementBuilder.from(Items.REPEATING_COMMAND_BLOCK).ui().build())
                .disabled(JSSTElementBuilder.from(Items.BLUE_TERRACOTTA).ui().build())
                .build(dev -> {
                    Sounds.UI.click(player);
                    this.session.setShowingDeveloperTools(dev);
                    this.refresh();
                }));
    }

    @Override
    protected void refresh() {
        this.setSlot(1, 1, JSSTElementBuilder.from(this.session.getStack())
                .leftClick(Translations.complete(), this::complete));

        List<WrappedElement<GuiElementInterface>> buttons = ItemEditor.EDITORS.stream()
                .filter(type -> type.appliesTo(this.session))
                .filter(type -> !type.isDeveloper() || this.session.isShowingDeveloperTools())
                .map(type -> WrappedElement.builder(type.getLabel(this.session))
                        // add id if dev mode is on
                        .addLore(this.session.isShowingDeveloperTools() ? List.of(Component.literal(type.getId().toString()).withStyle(Styles.MINOR_LABEL)) : List.of())
                        .leftClick(type.getInputHint(), () -> {
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

        if (this.isOpen()) {
            this.refresh();
        } else {
            this.open();
        }
    }
}
