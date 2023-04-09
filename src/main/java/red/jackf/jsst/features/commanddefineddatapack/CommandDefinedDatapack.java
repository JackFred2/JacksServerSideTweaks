package red.jackf.jsst.features.commanddefineddatapack;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class CommandDefinedDatapack extends Feature<CommandDefinedDatapack.Config> {
    private static final SimpleCommandExceptionType ERROR_DATAPACK_NOT_LOADED = new SimpleCommandExceptionType(Component.literal("Internal Datapack not loaded!"));
    public static final Logger LOGGER = LoggerFactory.getLogger("JSST CDD");

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
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        node.then(
            TagSubcommand.create(this)
        ).then(literal("save").executes(CommandUtils.wrapper(this).wrap(ctx -> {
            if (currentState == null) throw ERROR_DATAPACK_NOT_LOADED.create();
            currentState.save();
            CommandUtils.line(CommandUtils.TextType.SUCCESS, CommandUtils.text("Saved new datapack state."));
            return 1;
        })));
    }

    public static class Config extends Feature.Config {

    }
}
