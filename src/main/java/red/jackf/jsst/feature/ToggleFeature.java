package red.jackf.jsst.feature;

import blue.endless.jankson.Comment;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class ToggleFeature<C extends ToggleFeature.Config> extends Feature<C> {
    private boolean lastEnabled = false;

    public void enable() {}

    public void disable() {}

    @MustBeInvokedByOverriders
    public void reload(C current) {
        if (current.enabled != lastEnabled) {
            if (current.enabled) {
                enable();
            } else {
                disable();
            }
            lastEnabled = true;
        }
    }

    public static abstract class Config extends Feature.Config {
        @Comment("""
                Whether to enable this feature.
                Options: true, false
                Default: false""")
        public boolean enabled = true;
    }
}
