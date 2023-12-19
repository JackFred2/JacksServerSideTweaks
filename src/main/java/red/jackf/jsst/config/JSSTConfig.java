package red.jackf.jsst.config;

import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.config.Config;
import red.jackf.jackfredlib.api.config.error.ConfigValidationException;
import red.jackf.jsst.feature.beaconrange.ExtendedBeaconRange;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;

public class JSSTConfig implements Config<JSSTConfig> {
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();
    public ExtendedBeaconRange.Config extendedBeaconRange = new ExtendedBeaconRange.Config();

    @Override
    public void validate() throws ConfigValidationException {
        extendedBeaconRange.validate();
    }

    @Override
    public void onLoad(@Nullable JSSTConfig old) {
        PortableCrafting.INSTANCE.reload(this.portableCrafting, old != null ? old.portableCrafting : null);
        ExtendedBeaconRange.INSTANCE.reload(this.extendedBeaconRange, old != null ? old.extendedBeaconRange : null);
    }
}
