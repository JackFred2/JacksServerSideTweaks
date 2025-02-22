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
        public boolean enabled = false;

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
    public EffectorRanges effectorRanges = new EffectorRanges();

    public static class EffectorRanges {
        public float beaconRangeModifier = 1.5f;

        public float conduitRangeModifier = 1.5f;
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
    public ItemNudging itemNudging = new ItemNudging();

    public static class ItemNudging {
        public boolean shiftItemsUp = true;

        public boolean shiftItemsTowardsPlayer = true;
    }

    @SerialEntry
    public MapEditor mapEditor = new MapEditor();

    public static class MapEditor {
        public boolean enabled = true;

        public boolean requiresOp = false;

        public String tool = "minecraft:feather";

        public boolean disableSerialization = false;

        public boolean allowEditingLocked = false;
    }

    @SerialEntry
    public PortableCrafting portableCrafting = new PortableCrafting();

    public static class PortableCrafting {
        public boolean enabled = true;

        public boolean requiresSneak = false;

        public String itemIdOrTag = "#jsst:portable_crafting";
    }

    @SerialEntry
    public SaplingReplant saplingReplant = new SaplingReplant();

    public static class SaplingReplant {
        public boolean enabled = true;

        public String saplingTag = "minecraft:saplings";

        public int searchRadiusBlocks = 2;

        public int searchRadiusBlocks2x2 = 4;

        public int minSpacing = 2;

        public int maxPerStack = 4;
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

        float beaconRangeMod = Mth.clamp(instance.effectorRanges.beaconRangeModifier, 0.5f, 5f);
        if (instance.effectorRanges.beaconRangeModifier != beaconRangeMod) {
            modified = true;
            instance.effectorRanges.beaconRangeModifier = beaconRangeMod;
        }

        float conduitRangeMod = Mth.clamp(instance.effectorRanges.conduitRangeModifier, 0.5f, 5f);
        if (instance.effectorRanges.conduitRangeModifier != conduitRangeMod) {
            modified = true;
            instance.effectorRanges.conduitRangeModifier = conduitRangeMod;
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

        int saplingSearchRadius = Mth.clamp(instance.saplingReplant.searchRadiusBlocks, 0, 4);
        if (instance.saplingReplant.searchRadiusBlocks != saplingSearchRadius) {
            modified = true;
            instance.saplingReplant.searchRadiusBlocks = saplingSearchRadius;
        }

        int saplingSearchRadius2x2 = Mth.clamp(instance.saplingReplant.searchRadiusBlocks2x2, 0, 6);
        if (instance.saplingReplant.searchRadiusBlocks2x2 != saplingSearchRadius2x2) {
            modified = true;
            instance.saplingReplant.searchRadiusBlocks2x2 = saplingSearchRadius2x2;
        }

        int saplingMinSpacing = Mth.clamp(instance.saplingReplant.minSpacing, 0, 3);
        if (instance.saplingReplant.minSpacing != saplingMinSpacing) {
            modified = true;
            instance.saplingReplant.minSpacing = saplingMinSpacing;
        }

        int saplingMaxPerStack = Mth.clamp(instance.saplingReplant.maxPerStack, 1, 16);
        if (instance.saplingReplant.maxPerStack != saplingMaxPerStack) {
            modified = true;
            instance.saplingReplant.maxPerStack = saplingMaxPerStack;
        }

        if (modified) {
            INSTANCE.save();
        }
    }
}
