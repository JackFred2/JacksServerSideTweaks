package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapItemColor;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradients;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;
import red.jackf.jsst.impl.utils.sgui.menus.StringInputMenu;

import java.util.function.Consumer;

public class MapColourEditor implements Editor {
    public static Type<MapColourEditor> TYPE = Editor.<MapColourEditor>typeBuilder(JSST.id("map_colour"))
            .factory(MapColourEditor::new)
            .labelFactory(MapColourEditor::getIcon)
            .appliesTo(session -> session.getStack().is(Items.FILLED_MAP))
            .supportsCosmetic()
            .build();

    private final EditSession session;
    private final Consumer<Result> resultConsumer;

    public MapColourEditor(EditSession session, Consumer<Result> resultConsumer) {
        this.session = session;
        this.resultConsumer = resultConsumer;
    }

    private static GuiElementInterface getIcon(EditSession session) {
        var builder = new AnimatedGuiElementBuilderExt();

        for (int i = 0; i < 16; i++) {
            Colour col = Gradients.RAINBOW.sample(i / 16f);

            builder.addStack(JSSTElementBuilder.from(Items.FILLED_MAP).ui()
                    .setName(Component.translatable("jsst.itemEditor.editor.mapColour"))
                    .setComponent(DataComponents.MAP_COLOR, new MapItemColor(col.scaleBrightness(0.6f).toARGB()))
                    .asStack());
        }

        builder.setInterval(5);

        return builder.build();
    }

    @Override
    public void start() {
        Sounds.UI.click(this.session.getPlayer());
        InputMenus.colour(this.session.getPlayer())
                .appendOutput(StringInputMenu.AppendPriority.HIGH, (rawText, value, builder) ->
                        builder.setItem(JSSTElementBuilder.flatCopy(this.session.getStack())
                                .setComponent(DataComponents.MAP_COLOR, new MapItemColor(value.toARGB()))
                                .asStack()))
                .title(Component.translatable("jsst.itemEditor.editor.mapColour"))
                .start(opt -> {
                    if (opt.isPresent()) {
                        ItemStack stack = this.session.getStack();
                        stack.set(DataComponents.MAP_COLOR, new MapItemColor(opt.get().toARGB()));
                        this.resultConsumer.accept(Result.of(stack));
                    } else {
                        this.resultConsumer.accept(Result.empty());
                    }
                });
    }
}
