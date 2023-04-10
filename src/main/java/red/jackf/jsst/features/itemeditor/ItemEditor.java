package red.jackf.jsst.features.itemeditor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemEditor extends Feature<ItemEditor.Config> {

    @Override
    public void init() {

    }

    private void startEditor(CommandSourceStack source, ItemStack item) {
        var player = source.getPlayer();
        if (player == null) return;
        new EditSession(player, item.copyWithCount(1)).start();
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        var wrapper = CommandUtils.wrapper(this);
        node.then(literal("hand").executes(wrapper.wrap(ctx -> {
            var player = ctx.getSource().getPlayerOrException();
            var item = player.getMainHandItem();
            if (item.isEmpty()) item = player.getOffhandItem();
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("no item in hand!")));
                return 0;
            }
            startEditor(ctx.getSource(), item);
            return 1;
        }))).then(literal("item").then(argument("item", ItemArgument.item(buildContext)).executes(wrapper.wrap(ctx -> {
            var item = ItemArgument.getItem(ctx, "item").createItemStack(1, false);
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(CommandUtils.line(CommandUtils.TextType.ERROR, CommandUtils.text("empty item")));
                return 0;
            } else {
                startEditor(ctx.getSource(), item);
                return 1;
            }
        }))));
    }

    @Override
    public String id() {
        return "itemEditor";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().itemEditor;
    }

    public static class Config extends Feature.Config {}
}
