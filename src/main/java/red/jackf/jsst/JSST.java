package red.jackf.jsst;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.command.JSSTCommand;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.features.Feature;
import red.jackf.jsst.features.displayitems.DisplayItems;
import red.jackf.jsst.features.nbteditor.NBTEditor;
import red.jackf.jsst.features.portablecrafting.PortableCrafting;
import red.jackf.jsst.features.worldcontainernames.WorldContainerNames;

public class JSST implements ModInitializer {
    public static final String ID = "jsst";
    public static final Logger LOGGER = LoggerFactory.getLogger(JSST.class);
    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static final JSSTConfig.Handler CONFIG = new JSSTConfig.Handler();

    private static final Feature<?>[] features = new Feature[] {
            new PortableCrafting(),
            new WorldContainerNames(),
            new NBTEditor(),
            new DisplayItems()
    };

    @Override
    public void onInitialize() {
        CONFIG.load();
        LOGGER.info(features.length + " features");
        for (Feature<?> feature : features) {
            feature.init();
        }
        JSSTCommand.register(features);
    }
}
