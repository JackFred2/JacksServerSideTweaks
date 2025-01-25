package red.jackf.jsst.impl.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.beaconenhancement.BeaconPowers;

import java.util.HashSet;
import java.util.Set;

public class JSSTConfig {
    public static final ConfigClassHandler<JSSTConfig> INSTANCE = ConfigClassHandler.createBuilder(JSSTConfig.class)
            .id(JSST.id("config"))
            .serializer(handler -> GsonConfigSerializerBuilder.create(handler)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("jsst.json"))
                    .appendGsonBuilder(gson -> gson.setFieldNamingStrategy(FieldNamingPolicy.IDENTITY)
                            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocationAdapter())
                            .registerTypeAdapterFactory(new SetAsArrayAdapterFactory()))
                    .build())
            .build();

    @SerialEntry
    public BannerWriter bannerWriter = new BannerWriter();

    public static class BannerWriter {
        public boolean enabled = true;

        public boolean requiresOp = false;
    }

    @SerialEntry
    public BeaconEnhancement beaconEnhancement = new BeaconEnhancement();

    public static class BeaconEnhancement {
        public boolean enabled = true;

        public int maxLevel = 6;

        public boolean enableSecondPower = true;

        public int secondPowerMinLevel = 4;

        public BeaconPowers primaryPowers = BeaconPowers.DEFAULT_PRIMARY;

        public BeaconPowers secondaryPowers = BeaconPowers.DEFAULT_SECONDARY;
    }

    @SerialEntry
    public CampfireTimers campfireTimers = new CampfireTimers();

    public static class CampfireTimers {
        public boolean enabled = true;
    }

    @SerialEntry
    public ExtraHighlights extraHighlights = new ExtraHighlights();

    public static class ExtraHighlights {
        public boolean treeLogsEnabled = true;

        public int highlightTime = 10 * 20;

        public int treeLogsRange = 12;

        public String logsTag = "minecraft:logs";

        public String axesTag = "minecraft:axes";

        public boolean sugarcaneEnabled = true;
    }

    @SerialEntry
    public ItemEditor itemEditor = new ItemEditor();

    public static class ItemEditor {
        public boolean enabled = false;

        public boolean nonOpsCanUseCosmeticMode = false;

        public Set<ResourceLocation> disabledEditors = new HashSet<>();
    }

    @SerialEntry
    public MapEditor mapEditor = new MapEditor();

    public static class MapEditor {
        public boolean enabled = true;

        public boolean requiresOp = false;

        public String tool = "minecraft:feather";

        public boolean disableSerialization = false;
    }

    @SerialEntry
    public Miscellaneous miscellaneous = new Miscellaneous();

    public static class Miscellaneous {
        public boolean shiftItemsUp = true;

        public boolean shiftItemsTowardsPlayer = true;

        public float beaconRangeModifier = 1.5f;

        public float conduitRangeModifier = 1.5f;
    }

    @SerialEntry
    public PortableCrafting portableCrafting = new PortableCrafting();

    public static class PortableCrafting {
        public boolean enabled = true;

        public boolean requiresSneak = false;

        public String itemIdOrTag = "#jsst:portable_crafting";
    }

    public static void loadAndVerify() {
        INSTANCE.load();

        boolean modified = false;

        var instance = INSTANCE.instance();

        modified |= instance.itemEditor.disabledEditors.removeIf(id -> red.jackf.jsst.impl.feature.itemeditor.ItemEditor.EDITORS.stream()
                .noneMatch(type -> type.getId().equals(id)));

        int beaconMaxLevel = Mth.clamp(instance.beaconEnhancement.maxLevel, 1, 6);
        if (instance.beaconEnhancement.maxLevel != beaconMaxLevel) {
            modified = true;
            instance.beaconEnhancement.maxLevel = beaconMaxLevel;
        }

        int beaconSecondPowerLevel = Mth.clamp(instance.beaconEnhancement.secondPowerMinLevel, 1, 6);
        if (instance.beaconEnhancement.secondPowerMinLevel != beaconSecondPowerLevel) {
            modified = true;
            instance.beaconEnhancement.secondPowerMinLevel = beaconSecondPowerLevel;
        }

        float beaconRangeMod = Mth.clamp(instance.miscellaneous.beaconRangeModifier, 0.5f, 5f);
        if (instance.miscellaneous.beaconRangeModifier != beaconRangeMod) {
            modified = true;
            instance.miscellaneous.beaconRangeModifier = beaconRangeMod;
        }

        float conduitRangeMod = Mth.clamp(instance.miscellaneous.conduitRangeModifier, 0.5f, 5f);
        if (instance.miscellaneous.conduitRangeModifier != conduitRangeMod) {
            modified = true;
            instance.miscellaneous.conduitRangeModifier = conduitRangeMod;
        }

        int treeLogsRange = Mth.clamp(instance.extraHighlights.treeLogsRange, 6, 16);
        if (instance.extraHighlights.treeLogsRange != treeLogsRange) {
            modified = true;
            instance.extraHighlights.treeLogsRange = treeLogsRange;
        }

        int highlightLifetime = Mth.clamp(instance.extraHighlights.highlightTime, 20 * 5, 20 * 15);
        if (instance.extraHighlights.highlightTime != highlightLifetime) {
            modified = true;
            instance.extraHighlights.highlightTime = highlightLifetime;
        }

        if (modified) {
            INSTANCE.save();
        }
    }
}
