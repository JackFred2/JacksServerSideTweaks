package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.component.DyedItemColor;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;

import java.util.function.Consumer;

public class DyeColourEditor extends GuiEditor {
    public static final Type<DyeColourEditor> TYPE = Editor.<DyeColourEditor>typeBuilder(JSST.id("dye_colour"))
            .appliesTo(session -> session.getStack().is(ItemTags.DYEABLE))
            .labelFactory(DyeColourEditor::getLabel)
            .factory(DyeColourEditor::new)
            .supportsCosmetic()
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        return AnimatedGuiElementBuilderExt.makeForEach(ColourUtils.COLOURFUL_DYE_ORDER, col -> JSSTElementBuilder.from(DyeItem.byColor(col)).ui()
                    .setName(Component.translatable("jsst.itemEditor.editor.dyeColour"))
                    .asStack())
                .setInterval(4)
                .build();
    }

    public DyeColourEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.dyeColour"), MenuType.HOPPER, false);
    }

    @Override
    protected void drawStatic() {
        this.setSlot(1, CommonElements.divider());

        this.setSlot(3, CommonElements.clear(() -> {
            Sounds.UI.grind(player);
            this.stack.set(DataComponents.DYED_COLOR, this.stack.getPrototype().get(DataComponents.DYED_COLOR));
            this.refresh();
        }));

        this.setSlot(4, CommonElements.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        this.drawPreview(0);

        int currentColour = DyedItemColor.getOrDefault(this.stack, DyedItemColor.LEATHER_COLOR) & 0xFFFFFF;
        String asString = Integer.toHexString(currentColour).toUpperCase();
        this.setSlot(2, JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(Colour.fromInt(currentColour).closestDyeColour())).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.dyeColour.current", asString))
                .leftClick(Translations.change(), () -> {
                    Sounds.UI.click(player);

                    InputMenus.colour(player)
                            .initial("#" + asString)
                            .start(opt -> {
                                opt.ifPresent(col -> this.stack.set(DataComponents.DYED_COLOR, new DyedItemColor(col.toARGB(), true)));

                                this.open();
                            });
                }));
    }
}
