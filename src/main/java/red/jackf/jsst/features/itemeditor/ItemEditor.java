package red.jackf.jsst.features.itemeditor;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Feature;
import red.jackf.jsst.features.itemeditor.utils.LabelData;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemEditor extends Feature<ItemEditor.Config> {
    public static final Logger LOGGER = LoggerFactory.getLogger("JSST Item Editor");

    @Override
    public void init() {
        LabelData.setup();
    }

    private void startEditor(CommandSourceStack source, ItemStack item, @Nullable EquipmentSlot toReplace) {
        var player = source.getPlayer();
        if (player == null) return;
        new EditSession(player, item.copyWithCount(1), toReplace).start();
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        node.then(literal("hand").executes(ctx -> {
            var player = ctx.getSource().getPlayerOrException();
            var item = player.getMainHandItem();
            if (item.isEmpty()) item = player.getOffhandItem();
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("no item in hand!")));
                return 0;
            }
            startEditor(ctx.getSource(), item, player.getMainHandItem().isEmpty() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
            return 1;
        })).then(literal("item").then(argument("item", ItemArgument.item(buildContext)).executes(ctx -> {
            var item = ItemArgument.getItem(ctx, "item").createItemStack(1, false);
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("empty item")));
                return 0;
            } else {
                startEditor(ctx.getSource(), item, null);
                return 1;
            }
        })));

        node.then(
                OptionBuilders.withBoolean("enabledDevTools", () -> getConfig().enabledDevTools, newVal -> getConfig().enabledDevTools = newVal)
        );
    }

    @Override
    public String id() {
        return "itemEditor";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().itemEditor;
    }

    public static class Config extends Feature.Config {
        @Comment("Enable dev-specific editors")
        public boolean enabledDevTools = false;
    }
}
