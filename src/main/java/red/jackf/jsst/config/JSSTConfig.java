package red.jackf.jsst.config;

import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.bannerwriter.BannerWriter;
import red.jackf.jsst.features.beaconrangemodifier.BeaconRangeModifier;
import red.jackf.jsst.features.commanddefineddatapack.CommandDefinedDatapack;
import red.jackf.jsst.features.displayitems.DisplayItems;
import red.jackf.jsst.features.itemeditor.ItemEditor;
import red.jackf.jsst.features.portablecrafting.PortableCrafting;
import red.jackf.jsst.features.saplingsreplant.SaplingsReplant;
import red.jackf.jsst.features.worldcontainernames.WorldContainerNames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSSTConfig {
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();
    public WorldContainerNames.Config worldContainerNames = new WorldContainerNames.Config();
    public ItemEditor.Config itemEditor = new ItemEditor.Config();
    public DisplayItems.Config displayItems = new DisplayItems.Config();
    public CommandDefinedDatapack.Config commandDefinedDatapack = new CommandDefinedDatapack.Config();
    public BannerWriter.Config bannerWriter = new BannerWriter.Config();
    public SaplingsReplant.Config saplingsReplant = new SaplingsReplant.Config();
    public BeaconRangeModifier.Config beaconRangeModifier = new BeaconRangeModifier.Config();

    public static class Handler {
        private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("jsst.json5");

        private JSSTConfig instance = null;

        public JSSTConfig get() {
            if (instance == null) load();
            return instance;
        }

        public void load() {
            if (Files.exists(PATH)) {
                try {
                    var json = JSSTJankson.INSTANCE.load(PATH.toFile());
                    instance = JSSTJankson.INSTANCE.fromJson(json, JSSTConfig.class);
                } catch (IOException e) {
                    JSST.LOGGER.error("Couldn't read the config file", e);
                    instance = new JSSTConfig();
                    save();
                } catch (SyntaxError e) {
                    JSST.LOGGER.error(e.getMessage());
                    JSST.LOGGER.error(e.getLineMessage());
                    instance = new JSSTConfig();
                    save();
                }
                JSST.LOGGER.info("Loaded config.");
            } else {
                instance = new JSSTConfig();
                save();
            }
        }

        public void save() {
            var config = get();
            var json = JSSTJankson.INSTANCE.toJson(config);
            try {
                Files.writeString(PATH, json.toJson(JSSTJankson.GRAMMAR));
            } catch (IOException e) {
                JSST.LOGGER.error("Couldn't save the config file", e);
            }
        }
    }
}
