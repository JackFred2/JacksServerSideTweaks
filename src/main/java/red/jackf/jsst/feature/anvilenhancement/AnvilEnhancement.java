package red.jackf.jsst.feature.anvilenhancement;

import blue.endless.jankson.Comment;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.Feature;

public class AnvilEnhancement extends Feature<AnvilEnhancement.Config> {
    public static final AnvilEnhancement INSTANCE = new AnvilEnhancement();
    private AnvilEnhancement() {}

    @Override
    public void setup() {

    }

    @Override
    public Config config() {
        return JSST.CONFIG.instance().anvilEnhancement;
    }

    public enum RenameCost {
        free,
        one_level,
        vanilla
    }

    public static class Config extends Feature.Config {
        @Comment("""
                How to adjust the price of rename-only operations of items in an anvil.
                Options:
                  - vanilla: Same as vanilla (base work cost + 1 level).
                  - one_level: 1 level cost to rename items.
                  - free: Renaming items is free.
                Default: one_level""")
        public RenameCost renameCost = RenameCost.one_level;
    }
}
