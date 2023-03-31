package red.jackf.jsst.features.itemeditor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class ItemEditor extends Feature<ItemEditor.Config> {
    @Override
    public void init() {

    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(literal("hand").requires(unused -> getConfig().enabled).executes(ctx -> {
            ctx.getSource().sendSuccess(Component.literal("test"), false);
            return 0;
        }));
    }

    @Override
    public String id() {
        return "nbtEditor";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().itemEditor;
    }

    public static class Config extends Feature.Config {

    }
}
