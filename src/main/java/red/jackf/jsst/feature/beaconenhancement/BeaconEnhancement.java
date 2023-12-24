package red.jackf.jsst.feature.beaconenhancement;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

public class BeaconEnhancement extends ToggleFeature<BeaconEnhancement.Config> {
    public static final BeaconEnhancement INSTANCE = new BeaconEnhancement();
    private BeaconEnhancement() {}

    @Override
    public void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            BlockEntity be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof BeaconBlockEntity bbe && config().enabled) {
                new ExpandedBeaconScreen(serverPlayer, bbe).open();
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        });
    }

    @Override
    public Config config() {
        return JSST.CONFIG.instance().beaconEnhancement;
    }

    public static class Config extends ToggleFeature.Config {
        @Comment("""
                Multiplier for the beacon's range. This is a radius of 10 blocks, plus 10 for every level. For reference,
                a level 4 beacon in vanilla has a radius of 50 blocks.
                Options: [0.5, 8]
                Default: 1.5""")
        public double rangeMultiplier = 1.5;

        @Comment("""
                This feature can optionally increase the maximum level of the beacon. This allows you to limit more powerful effects
                to higher levels.
                Options: [4, 6]
                Default: 6""")
        public int maxBeaconLevel = 6;

        @Comment("""
                List of powers available at each tier of the beacon. The levels available are 1 until 6 compared to vanilla's 1 to 4;
                this is limited by `maxBeaconLevel` above. There can only be 1 instance of a given effect in the 1-3 range or the 4-6 range.
                Options: A map of beacon levels to a set of available status effects.
                Default: Same as vanilla's beacon up to 4; slow falling and night vision at 5; fire resistance and health boost at 6""")
        public BeaconPowerSet powers = BeaconPowerSet.getDefault();

        public void validate() {
            this.rangeMultiplier = Mth.clamp(this.rangeMultiplier, 0.5, 8);
            this.maxBeaconLevel = Mth.clamp(this.maxBeaconLevel, 4, 6);
        }
    }
}
