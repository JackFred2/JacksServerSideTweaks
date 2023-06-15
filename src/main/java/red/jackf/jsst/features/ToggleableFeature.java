package red.jackf.jsst.features;

import blue.endless.jankson.Comment;

public abstract class ToggleableFeature<C extends ToggleableFeature.Config> extends Feature<C> {
    /**
     * Called when this feature is enabled.
     */
    public void onEnabled() {}

    /**
     * Called when this feature is disabled.
     */
    public void onDisabled() {}

    @Override
    public abstract C getConfig();

    public static abstract class Config extends Feature.Config {
        @Comment("Is this feature enabled? (Default: true, Options: true, false)")
        public boolean enabled = true;
    }
}
