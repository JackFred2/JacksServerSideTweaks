package red.jackf.jsst.feature;

public abstract class Feature<C extends Feature.Config> {
    public abstract void setup();

    public void reload(C current) {}

    protected abstract C config();

    public static abstract class Config {}
}
