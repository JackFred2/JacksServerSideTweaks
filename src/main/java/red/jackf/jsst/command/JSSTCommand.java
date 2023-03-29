package red.jackf.jsst.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class JSSTCommand {
    public static void register(Feature<?>[] features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = dispatcher.register(literal(("jsst")));

            for (Feature<?> feature : features) {
                var node = literal(feature.id());

                OptionBuilders.addEnabled(node, feature);

                feature.setupCommand(node);

                root.addChild(node.build());
            }
        });
    }
}
