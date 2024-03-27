package red.jackf.jsst.feature.bannerwriter;

import blue.endless.jankson.Comment;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.Feature;

public class BannerWriter extends Feature<BannerWriter.Config> {
    public static final BannerWriter INSTANCE = new BannerWriter();

    private BannerWriter() {}

    @Override
    public void setup() {

    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().bannerWriter;
    }

    public static class Config extends Feature.Config {
        @Comment("""
                Whether the banner writer command should be available only to operators, or to anyone.
                TODO: Add permissions node
                Options: true, false
                Default: false""")
        public boolean operatorOnly = false;

        @Comment("""
                Adds a dedicated /bannerWriter command to use the banner writer as an alternative to /jsst bannerWriter.
                Requires a server restart or world reload to take effect.
                Options: true, false
                Default: true""")
        public boolean dedicatedCommand = true;
    }
}
