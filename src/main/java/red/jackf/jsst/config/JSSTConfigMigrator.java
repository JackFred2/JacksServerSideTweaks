package red.jackf.jsst.config;

import red.jackf.jackfredlib.api.config.migration.MigratorBuilder;

public class JSSTConfigMigrator {
    public static MigratorBuilder<JSSTConfig> get() {
        return MigratorBuilder.forMod("jsst");
    }
}
