package red.jackf.jsst.features.commanddefineddatapack;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

public class CommandDefinedDatapack extends Feature<CommandDefinedDatapack.Config> {
    @Nullable
    protected static PackState currentState = null;

    @Override
    public void init() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resmon, success) -> currentState = PackState.load(server));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentState = PackState.load(server));
    }

    @Override
    public String id() {
        return "commandDefinedDatapack";
    }

    @Override
    public String commandLabel() {
        return "cdd";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().commandDefinedDatapack;
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(
            TagSubcommand.create()
        );
    }

    public static class Config extends Feature.Config {

    }
}
