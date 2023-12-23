package red.jackf.jsst.config;

import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.config.Config;
import red.jackf.jsst.feature.beaconpowers.MoreBeaconPowers;
import red.jackf.jsst.feature.beaconrange.ExtendedBeaconRange;
import red.jackf.jsst.feature.containernames.WorldContainerNames;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;

public class JSSTConfig implements Config<JSSTConfig> {
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();
    public ExtendedBeaconRange.Config extendedBeaconRange = new ExtendedBeaconRange.Config();
    public WorldContainerNames.Config worldContainerNames = new WorldContainerNames.Config();
    public MoreBeaconPowers.Config moreBeaconPowers = new MoreBeaconPowers.Config();

    @Override
    public void validate() {
        extendedBeaconRange.validate();
        worldContainerNames.validate();
    }

    @Override
    public void onLoad(@Nullable JSSTConfig old) {
        PortableCrafting.INSTANCE.reload(this.portableCrafting, old != null ? old.portableCrafting : null);
        ExtendedBeaconRange.INSTANCE.reload(this.extendedBeaconRange, old != null ? old.extendedBeaconRange : null);
        WorldContainerNames.INSTANCE.reload(this.worldContainerNames, old != null ? old.worldContainerNames : null);
        MoreBeaconPowers.INSTANCE.reload(this.moreBeaconPowers, old != null ? old.moreBeaconPowers : null);
    }
}
