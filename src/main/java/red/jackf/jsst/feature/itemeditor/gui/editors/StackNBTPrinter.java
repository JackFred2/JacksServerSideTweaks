package red.jackf.jsst.feature.itemeditor.gui.editors;

import com.mojang.serialization.JsonOps;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.command.Formatting;
import red.jackf.jsst.util.JSSTCodecs;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Styles;

import java.util.function.Consumer;

public class StackNBTPrinter implements Editor {
    public static EditorType TYPE = new EditorType(
            StackNBTPrinter::new,
            true,
            ignored -> true,
            () -> GuiElementBuilder.from(Items.ACACIA_SIGN.getDefaultInstance())
                                   .setName(Component.translatable("jsst.itemEditor.labelMapNBTPrinter"))
                                   .addLoreLine(Component.translatable("jsst.itemEditor.labelMapNBTPrinter.hint").withStyle(Styles.MINOR_LABEL))
    );
    private final ServerPlayer player;
    private final ItemStack stack;

    public StackNBTPrinter(
            ServerPlayer player,
            boolean cosmeticOnly,
            ItemStack stack,
            Consumer<ItemStack> callback) {
        this.player = player;
        this.stack = stack;
    }

    @Override
    public void run() {
        Sounds.click(player);
        String str = Util.getOrThrow(JSSTCodecs.SIMPLIFIED_ITEMSTACK.encodeStart(JsonOps.INSTANCE, this.stack), IllegalStateException::new)
                         .toString();
        HoverEvent action = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(str).withStyle(Styles.LABEL));
        MutableComponent text = Component.translatable("jsst.itemEditor.labelMapNBTPrinter.copyInstruction")
                                         .withStyle(Styles.INPUT_HINT.withHoverEvent(action)
                                                                     .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)));
        this.player.sendSystemMessage(Formatting.successLine(text));
        this.player.closeContainer();
    }
}