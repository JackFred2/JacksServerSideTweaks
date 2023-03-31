package red.jackf.jsst.features.nbteditor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

import static net.minecraft.commands.Commands.literal;

public class NBTEditor extends Feature<NBTEditor.Config> {
    @Override
    public void init() {

    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(literal("hand").executes(ctx -> {
            return 0;
        }));
    }

    @Override
    public String id() {
        return "nbtEditor";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().nbtEditor;
    }

    public static class Config extends Feature.Config {

    }
}
