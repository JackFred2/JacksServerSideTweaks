package red.jackf.jsst;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.command.JSSTCommand;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.features.Feature;
import red.jackf.jsst.features.bannerwriter.BannerWriter;
import red.jackf.jsst.features.commanddefineddatapack.CommandDefinedDatapack;
import red.jackf.jsst.features.displayitems.DisplayItems;
import red.jackf.jsst.features.itemeditor.ItemEditor;
import red.jackf.jsst.features.portablecrafting.PortableCrafting;
import red.jackf.jsst.features.saplingsreplant.SaplingsReplant;
import red.jackf.jsst.features.worldcontainernames.WorldContainerNames;
import red.jackf.jsst.util.DelayedRunnables;

import java.util.ArrayList;
import java.util.List;

public class JSST implements ModInitializer {
    public static final String ID = "jsst";
    public static final Logger LOGGER = LoggerFactory.getLogger(JSST.class);
    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static final JSSTConfig.Handler CONFIG = new JSSTConfig.Handler();

    private static final List<Feature<?>> features = new ArrayList<>();
    static {
        features.add(new PortableCrafting());
        features.add(new WorldContainerNames());
        features.add(new DisplayItems());
        features.add(new CommandDefinedDatapack());
        features.add(new ItemEditor());
        features.add(new BannerWriter());
        features.add(new SaplingsReplant());
    }

    @Override
    public void onInitialize() {
        CONFIG.load();
        LOGGER.info(features.size() + " features");
        for (Feature<?> feature : features) {
            feature.init();
        }
        JSSTCommand.register(features);
        DelayedRunnables.setup();
    }
}
