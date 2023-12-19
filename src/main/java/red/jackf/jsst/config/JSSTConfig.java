package red.jackf.jsst.config;

import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.config.Config;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;

public class JSSTConfig implements Config<JSSTConfig> {
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();

    @Override
    public void onLoad(@Nullable JSSTConfig old) {
        PortableCrafting.INSTANCE.reload(this.portableCrafting, old != null ? old.portableCrafting : null);
    }
}
