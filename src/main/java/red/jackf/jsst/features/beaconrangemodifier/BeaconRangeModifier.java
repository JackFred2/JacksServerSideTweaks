package red.jackf.jsst.features.beaconrangemodifier;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.ToggleableFeature;

public class BeaconRangeModifier extends ToggleableFeature<BeaconRangeModifier.Config> {

    @Override
    public void init() {
        getConfig().rangeMultiplier = Mth.clamp(getConfig().rangeMultiplier, 0.5f, 8f);
    }

    @Override
    public String id() {
        return "beaconRangeModifier";
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        super.setupCommand(node, buildContext);
        node.then(OptionBuilders.withFloatRange("rangeMultiplier", 0.5f, 8f, () -> getConfig().rangeMultiplier, newRange -> getConfig().rangeMultiplier = newRange));
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().beaconRangeModifier;
    }

    public static class Config extends ToggleableFeature.Config {
        @Comment("Multiplier for the range on beacons. (Default: 1.5, Range: [0.5, 8])")
        public float rangeMultiplier = 1.5f;
    }
}
