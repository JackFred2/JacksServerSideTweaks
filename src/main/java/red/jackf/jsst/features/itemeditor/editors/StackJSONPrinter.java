package red.jackf.jsst.features.itemeditor.editors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.itemeditor.ItemEditor;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

public class StackJSONPrinter extends Editor {
    private static final Gson GSON = new GsonBuilder().create();
    private static final int MAX_PRINTED = 150;
    public StackJSONPrinter(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public void playOpenSound() {}

    @Override
    public boolean applies(ItemStack stack) {
        return JSST.CONFIG.get().itemEditor.enabledDevTools;
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.ACACIA_SIGN).withName("Copy Stack JSON").build();
    }

    @Override
    public void open() {
        var result = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack);
        result.resultOrPartial(s -> {
            player.sendSystemMessage(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("could not convert to JSON; check console")));
            ItemEditor.LOGGER.error("Error converting stack to JSON: %s".formatted(s));
            cancel();
        }).ifPresentOrElse(json -> {
            var str = GSON.toJson(json);
            var style = Labels.CLEAN.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str));
            player.sendSystemMessage(CommandUtils.line(CommandUtils.TextType.SUCCESS, CommandUtils.text(str.length() > MAX_PRINTED ? str.substring(0, MAX_PRINTED) + "..." : str, style)));
            complete();
        }, () -> {
            player.sendSystemMessage(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("could not convert to JSON; no JSON returned")));
            ItemEditor.LOGGER.error("Error converting stack %s to JSON: no JSON returned from encode".formatted(stack));
            cancel();
        });
    }
}
