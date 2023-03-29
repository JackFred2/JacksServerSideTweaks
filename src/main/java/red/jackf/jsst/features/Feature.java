package red.jackf.jsst.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandResponse;

public abstract class Feature<C extends Feature.Config> {
    public abstract void init();

    public abstract String id();

    public abstract C getConfig();

    public CommandResponse enable() {
        if (!getConfig().enabled) {
            getConfig().enabled = true;
            return CommandResponse.OK;
        } else {
            return CommandResponse.NO_CHANGE;
        }
    }

    public CommandResponse disable() {
        if (getConfig().enabled) {
            getConfig().enabled = false;
            return CommandResponse.OK;
        } else {
            return CommandResponse.NO_CHANGE;
        }
    }

    public boolean isEnabled() {
        return JSST.CONFIG.get().portableCrafting.enabled;
    }

    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {}

    public static abstract class Config {
        public boolean enabled = true;
    }
}
