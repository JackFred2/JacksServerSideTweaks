package red.jackf.jsst;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jsst.command.JSSTConfigCommand;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.features.PortableCraftingTable;

public class JSST implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("jsst");
    public static final JSSTConfig.Handler CONFIG_HANDLER = new JSSTConfig.Handler();

    @Override
    public void onInitialize() {
        CONFIG_HANDLER.load();
        CommandRegistrationCallback.EVENT.register(JSSTConfigCommand::register);
        PortableCraftingTable.setup();
    }
}
