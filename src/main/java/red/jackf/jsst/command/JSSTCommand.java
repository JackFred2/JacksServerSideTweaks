package red.jackf.jsst.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import red.jackf.jsst.feature.itemeditor.ItemEditorCommand;

public class JSSTCommand {
    public static void create(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            Commands.CommandSelection ignored) {
        var root = Commands.literal("jsst");

        root.requires(ctx -> ctx.hasPermission(4));

        root.then(CommandConfig.createCommandNode(buildContext));
        root.then(ItemEditorCommand.create(buildContext));

        dispatcher.register(root);
    }
}
