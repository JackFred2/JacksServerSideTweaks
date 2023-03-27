package red.jackf.jsst.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class JSSTCommand {
    private static void sendEnabledMessages(CommandSourceStack source, Response response, String featureName, boolean isEnabled) {
        var word = isEnabled ? "enabled" : "disabled";
        if (response == Response.NO_CHANGE)
            source.sendFailure(Component.literal(featureName + " already " + word + "!"));
        else {
            source.sendSuccess(Component.literal(featureName + " " + word + ".").withStyle(ChatFormatting.GREEN), true);
            if (response == Response.RESTART_REQUIRED)
                source.sendSystemMessage(Component.literal("A restart may be required to take full effect.").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void register(Feature[] features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = dispatcher.register(literal(("jsst")));

            for (Feature feature : features) {
                var node = literal(feature.id())
                        .then(literal("enable").executes(ctx -> {
                            var response = feature.enable();
                            sendEnabledMessages(ctx.getSource(), response, feature.prettyName(), true);
                            return 1;
                        })).then(literal("disable").executes(ctx -> {
                            var response = feature.disable();
                            sendEnabledMessages(ctx.getSource(), response, feature.prettyName(), false);
                            return 1;
                        }));

                root.addChild(node.build());
            }
        });
    }
}
