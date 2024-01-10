package red.jackf.jsst.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import red.jackf.jackfredlib.api.base.ServerTracker;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.ItemEditorCommand;

import java.util.function.Predicate;

public class JSSTCommand {
    private static final Predicate<CommandSourceStack> CONFIG_PREDICATE = ctx -> ctx.hasPermission(4);


    public static void create(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            Commands.CommandSelection ignored) {
        var root = Commands.literal("jsst");

        root.requires(CONFIG_PREDICATE.or(ItemEditorCommand.PREDICATE));

        root.then(CommandConfig.createCommandNode(buildContext)
                               .requires(CONFIG_PREDICATE));

        var itemEditor = ItemEditorCommand.create(buildContext)
                                          .requires(ItemEditorCommand.PREDICATE);

        root.then(itemEditor);

        if (JSST.CONFIG.instance().itemEditor.dedicatedCommand) dispatcher.register(itemEditor);

        dispatcher.register(root);
    }

    public static void resendCommands() {
        var server = ServerTracker.INSTANCE.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(server.getCommands()::sendCommands);
        }
    }
}
