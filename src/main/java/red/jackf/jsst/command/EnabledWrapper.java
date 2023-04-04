package red.jackf.jsst.command;

import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import red.jackf.jsst.features.Feature;

public record EnabledWrapper(Feature<?> feature) {
    public Command<CommandSourceStack> wrap(Command<CommandSourceStack> command) {
        return ctx -> {
            feature.assertEnabled();
            return command.run(ctx);
        };
    }
}
