package red.jackf.jsst.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.config.JSSTConfig;

public abstract class Feature<C extends Feature.Config> {
    /**
     * Called to set up hooks and events for this feature.
     */
    public abstract void init();

    /**
     * Identifier for this feature, used in commands. Should be <code>camelCaseOnly</code>.
     * @return Feature Identifier
     */
    public abstract String id();

    public String commandLabel() {
        return id();
    }

    /**
     * Returns this feature's current config from {@link JSSTConfig.Handler#get()}.
     * @return This feature's current config.
     */
    public abstract C getConfig();

    /**
     * Add any nodes to this feature's command here. Helpers for config options are available in {@link OptionBuilders}.
     * @param node this feature's root node.
     */
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {}

    public static abstract class Config {
    }
}
