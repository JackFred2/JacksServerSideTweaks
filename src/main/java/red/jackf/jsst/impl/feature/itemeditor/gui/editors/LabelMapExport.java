package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import com.mojang.serialization.JsonOps;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.ModCodecs;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.function.Consumer;

public class LabelMapExport implements Editor {
    public static final Type<LabelMapExport> TYPE = new Type<>(
            JSST.id("label_map_export"),
            LabelMapExport::new,
            session -> true,
            LabelMapExport::getLabel
    );

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.SPRUCE_SIGN).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.labelMapExport"))
                .build();
    }

    private final EditSession session;
    private final Consumer<Result> resultConsumer;

    public LabelMapExport(EditSession session, Consumer<Result> resultConsumer) {
        this.session = session;
        this.resultConsumer = resultConsumer;
    }

    @Override
    public void start() {
        Sounds.UI.click(session.getPlayer());

        ItemStack cleaned = session.getStack();
        cleaned.remove(DataComponents.CUSTOM_NAME);

        ModCodecs.POSSIBLY_SIMPLE_STACK.encodeStart(session.registries().createSerializationContext(JsonOps.INSTANCE), cleaned)
                .ifSuccess(json -> {
                    String str = json.toString();

                    Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str));

                    this.session.getPlayer().sendSystemMessage(Component.literal(str).withStyle(style));
                });

        this.resultConsumer.accept(Result.empty());
    }
}
