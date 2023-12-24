package red.jackf.jsst.config;

import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.config.Config;
import red.jackf.jsst.feature.beaconenhancement.BeaconEnhancement;
import red.jackf.jsst.feature.containernames.WorldContainerNames;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;

public class JSSTConfig implements Config<JSSTConfig> {
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();
    public WorldContainerNames.Config worldContainerNames = new WorldContainerNames.Config();
    public BeaconEnhancement.Config beaconEnhancement = new BeaconEnhancement.Config();

    @Override
    public void validate() {
        worldContainerNames.validate();
        beaconEnhancement.validate();
    }

    @Override
    public void onLoad(@Nullable JSSTConfig old) {
        PortableCrafting.INSTANCE.reload(this.portableCrafting);
        WorldContainerNames.INSTANCE.reload(this.worldContainerNames);
        BeaconEnhancement.INSTANCE.reload(this.beaconEnhancement);
    }
}
