package red.jackf.jsst.feature;

import org.jetbrains.annotations.Nullable;

public abstract class Feature<C extends Feature.Config> {
    public abstract void setup();

    public abstract void reload(C current, @Nullable C old);

    protected abstract C config();

    public static abstract class Config {}
}
