package red.jackf.jsst.feature;

import blue.endless.jankson.Comment;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class ToggleFeature<C extends ToggleFeature.Config> extends Feature<C> {
    public void enable() {}

    public void disable() {}

    @MustBeInvokedByOverriders
    @Override
    public void reload(C current, @Nullable C old) {
        if (old == null || old.enabled != current.enabled) {
            if (current.enabled) {
                enable();
            } else {
                disable();
            }
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
