package red.jackf.jsst;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jackfredlib.api.config.ConfigHandler;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.config.JSSTConfigMigrator;
import red.jackf.jsst.feature.Feature;
import red.jackf.jsst.feature.ToggleFeature;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;

import java.util.List;

public class JSST implements ModInitializer {
    public static Logger getLogger(String suffix) {
        return LoggerFactory.getLogger("red.jackf.jsst.JSST" + (suffix.isBlank() ? "" : "/" + suffix));
    }
    public static final Logger LOGGER = getLogger("");

    public static final String MODID = "jsst";
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static final ConfigHandler<JSSTConfig> CONFIG = ConfigHandler.builder(JSSTConfig.class)
            .fileName(MODID)
            .withFileWatcher()
            .withLogger(getLogger("Config"))
            .withMigrator(JSSTConfigMigrator.get())
            .build();

    private static final List<Feature<?>> FEATURES = List.of(
            PortableCrafting.INSTANCE
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Features: {} ({} Toggleable)", FEATURES.size(), FEATURES.stream().filter(feature -> feature instanceof ToggleFeature<?>).count());
        for (Feature<?> feature : FEATURES) {
            feature.setup();
        }
        CONFIG.save();
    }
}
