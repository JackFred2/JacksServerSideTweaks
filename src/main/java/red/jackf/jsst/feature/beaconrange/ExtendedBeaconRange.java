package red.jackf.jsst.feature.beaconrange;

import blue.endless.jankson.Comment;
import net.minecraft.util.Mth;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

public class ExtendedBeaconRange extends ToggleFeature<ExtendedBeaconRange.Config> {
    public static final ExtendedBeaconRange INSTANCE = new ExtendedBeaconRange();
    private ExtendedBeaconRange() {}

    @Override
    public void setup() {}

    public double getMultiplier() {
        return config().enabled ? config().rangeMultiplier : 1;
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().extendedBeaconRange;
    }

    public static class Config extends ToggleFeature.Config {
        @Comment("""
                Multiplier for the beacon's range. For reference, a max level beacon in vanilla is 50 blocks radius.
                Options: [0.5, 8]
                Default: 1.5""")
        public double rangeMultiplier = 1.5;

        public void validate() {
            this.rangeMultiplier = Mth.clamp(this.rangeMultiplier, 0.5, 8);
        }
    }
}
