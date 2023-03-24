package red.jackf.jsst;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.features.Feature;
import red.jackf.jsst.features.worldcontainernames.WorldContainerNames;
import red.jackf.jsst.features.portablecrafting.PortableCrafting;

public class JSST implements ModInitializer {
    public static final String ID = "jsst";
    public static final Logger LOGGER = LoggerFactory.getLogger(JSST.class);
    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    private static final Feature[] features = new Feature[] {
            new PortableCrafting(),
            new WorldContainerNames()
    };

    @Override
    public void onInitialize() {
        LOGGER.info(features.length + " features");
        for (Feature feature : features) {
            feature.init();
        }
    }
}
