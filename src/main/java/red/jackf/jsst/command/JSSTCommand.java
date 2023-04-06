package red.jackf.jsst.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import java.util.ArrayList;
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
                    var str = new ArrayList<CommandUtils.Text>();
                    for (int i = 0; i < enabled.size(); i++) {
                        if (i > 0) str.add(CommandUtils.symbol(", "));
                        str.add(CommandUtils.text(enabled.get(i).id()));
                    }
                    ctx.getSource().sendSuccess(CommandUtils.line(CommandUtils.TextType.SUCCESS, str), false);
                }

                if (disabled.size() > 0) {
                    var str = new ArrayList<CommandUtils.Text>();
                    for (int i = 0; i < disabled.size(); i++) {
                        if (i > 0) str.add(CommandUtils.symbol(", "));
                        str.add(CommandUtils.text(disabled.get(i).id()));
                    }
                    ctx.getSource().sendSuccess(CommandUtils.line(CommandUtils.TextType.ERROR, str), false);
                }
                return 1;
            });

            root.requires(source -> source.isPlayer() && source.hasPermission(3));

            dispatcher.register(root);
        });
    }
}
