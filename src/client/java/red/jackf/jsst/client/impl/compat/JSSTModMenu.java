package red.jackf.jsst.client.impl.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import red.jackf.jsst.client.impl.config.JSSTConfigScreen;

public class JSSTModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return JSSTConfigScreen::create;
    }
}
