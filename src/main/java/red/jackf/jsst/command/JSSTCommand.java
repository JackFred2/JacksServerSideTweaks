package red.jackf.jsst.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.literal;

public class JSSTCommand {
    public static void register(List<Feature<?>> features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = literal(JSST.ID);

            for (Feature<?> feature : features) {
                var node = literal(feature.commandLabel());

                OptionBuilders.addEnabled(node, feature);

                feature.setupCommand(node);

                root.then(node);
            }

            root.executes(ctx -> {
                var map = features.stream().collect(Collectors.partitioningBy(feature -> feature.getConfig().enabled));
                var enabled = map.get(true);
                var disabled = map.get(false);
                if (enabled.size() > 0) {
                    var str = CommandUtils.sucessPrefix();
                    for (int i = 0; i < enabled.size(); i++) {
                        if (i > 0) str.append(Component.literal(", ").withStyle(ChatFormatting.GREEN));
                        str.append(Component.literal(enabled.get(i).id()).withStyle(ChatFormatting.WHITE));
                    }
                    ctx.getSource().sendSuccess(str, false);
                }
                if (disabled.size() > 0) {
                    var str = CommandUtils.errorPrefix();
                    for (int i = 0; i < disabled.size(); i++) {
                        if (i > 0) str.append(Component.literal(", ").withStyle(ChatFormatting.RED));
                        str.append(Component.literal(disabled.get(i).id()).withStyle(ChatFormatting.WHITE));
                    }
                    ctx.getSource().sendSuccess(str, false);
                }
                return 1;
            });

            root.requires(source -> source.isPlayer() && source.hasPermission(3));

            dispatcher.register(root);
        });
    }
}
