package red.jackf.jsst.config;

import blue.endless.jankson.Comment;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.config.Config;
import red.jackf.jsst.feature.anvilenhancement.AnvilEnhancement;
import red.jackf.jsst.feature.bannerwriter.BannerWriter;
import red.jackf.jsst.feature.beaconenhancement.BeaconEnhancement;
import red.jackf.jsst.feature.campfiretimes.CampfireTimes;
import red.jackf.jsst.feature.worldcontainernames.WorldContainerNames;
import red.jackf.jsst.feature.itemeditor.ItemEditor;
import red.jackf.jsst.feature.portablecrafting.PortableCrafting;
import red.jackf.jsst.feature.qualityoflife.QualityOfLife;

public class JSSTConfig implements Config<JSSTConfig> {
    @Comment("""
            Tweaks to the anvil to make them less frustrating to use; mainly removing the XP cost and damage from renaming
            items.""")
    public AnvilEnhancement.Config anvilEnhancement = new AnvilEnhancement.Config();

    @Comment("""
            Adds a command to easily write text on banners in-game.""")
    public BannerWriter.Config bannerWriter = new BannerWriter.Config();

    @Comment("""
            Allows beacon effects and maximum level to be customized. Replaces the vanilla Beacon GUI to accommodate this.""")
    public BeaconEnhancement.Config beaconEnhancement = new BeaconEnhancement.Config();

    @Comment("""
            Adds text above campfire items detailing how much time is left.""")
    public CampfireTimes.Config campfireTimes = new CampfireTimes.Config();

    @Comment("""
            Adds a powerful in-game item editor, accessed using /jsst itemEditor. Offers a cosmetic-only mode for survival
            flavour.""")
    public ItemEditor.Config itemEditor = new ItemEditor.Config();

    @Comment("""
            Allows players to right click with a crafting table in-hand to open it, instead of needing to place it down. Configurable
            using a tag representing crafting table items.""")
    public PortableCrafting.Config portableCrafting = new PortableCrafting.Config();

    @Comment("""
            Tiny gameplay mechanics tweaks to reduce frustration when playing.""")
    public QualityOfLife.Config qol = new QualityOfLife.Config();

    @Comment("""
            Gives all named containers (chests, barrels, shulker boxes) a visible name in-world. Can optionally display an item stack
            instead.""")
    public WorldContainerNames.Config worldContainerNames = new WorldContainerNames.Config();

    @Override
    public void validate() {
        worldContainerNames.validate();
        beaconEnhancement.validate();
    }

    @Override
    public void onLoad(@Nullable JSSTConfig old) {
        AnvilEnhancement.INSTANCE.reload(this.anvilEnhancement);
        BannerWriter.INSTANCE.reload(this.bannerWriter);
        BeaconEnhancement.INSTANCE.reload(this.beaconEnhancement);
        CampfireTimes.INSTANCE.reload(this.campfireTimes);
        ItemEditor.INSTANCE.reload(this.itemEditor);
        PortableCrafting.INSTANCE.reload(this.portableCrafting);
        QualityOfLife.INSTANCE.reload(this.qol);
        WorldContainerNames.INSTANCE.reload(this.worldContainerNames);
    }
}
