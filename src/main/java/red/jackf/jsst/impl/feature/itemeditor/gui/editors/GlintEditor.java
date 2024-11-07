package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.Objects;
import java.util.function.Consumer;

public class GlintEditor implements Editor {
    public static final Type<GlintEditor> TYPE = Editor.<GlintEditor>typeBuilder(JSST.id("glint"))
            .factory(GlintEditor::new)
            .labelFactory(GlintEditor::getLabel)
            .inputHint(Translations::change)
            .supportsCosmetic()
            .build();

    private static @Nullable Boolean cycle(@Nullable Boolean value) {
        if (value == null) return true;
        else if (value) return false;
        else return null;
    }

    private static GuiElementInterface getLabel(EditSession session) {
        Boolean current = session.getStack().get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        Boolean next = cycle(current);

        var builder = JSSTElementBuilder.from(session.getStack()).ui()
                .hideDefaultTooltip()
                .removeComponent(DataComponents.LORE)
                .setName(Component.translatable("jsst.itemEditor.editor.glint"))
                .addLoreLine(Component.translatable("jsst.itemEditor.editor.glint." + Objects.toString(current, "default")).withStyle(Styles.LABEL))
                .glow(next);

        return builder.build();
    }

    private final EditSession session;
    private final Consumer<Result> resultConsumer;

    public GlintEditor(EditSession session, Consumer<Result> resultConsumer) {
        this.session = session;
        this.resultConsumer = resultConsumer;
    }

    @Override
    public void start() {
        Sounds.UI.click(this.session.getPlayer());

        ItemStack stack = this.session.getStack();
        stack.update(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, null, GlintEditor::cycle);

        this.resultConsumer.accept(Result.of(stack));
    }
}
