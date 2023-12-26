package red.jackf.jsst.feature.qualityoflife;

import blue.endless.jankson.Comment;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.Feature;

public class QualityOfLife extends Feature<QualityOfLife.Config> {
    public static final QualityOfLife INSTANCE = new QualityOfLife();
    private QualityOfLife() {}

    @Override
    public void setup() {
        
    }

    @Override
    public Config config() {
        return JSST.CONFIG.instance().qol;
    }

    public static class Config extends Feature.Config {
        @Comment("""
                Whether blocks mined will be moved up enough to be picked up from above. Convenient for Skyblocks.
                Options: true, false
                Default: true""")
        public boolean doMinedItemsShiftUp = true;

        @Comment("""
                Whether blocks mined will move towards.
                Options: true, false
                Default: true""")
        public boolean doMinedItemsShiftTowardsPlayer = true;
    }
}
