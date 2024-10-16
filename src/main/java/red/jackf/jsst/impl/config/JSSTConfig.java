package red.jackf.jsst.impl.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import red.jackf.jsst.impl.JSST;

public class JSSTConfig {
    public static final ConfigClassHandler<JSSTConfig> INSTANCE = ConfigClassHandler.createBuilder(JSSTConfig.class)
            .id(JSST.id("config"))
            .serializer(handler -> GsonConfigSerializerBuilder.create(handler)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("jsst.json"))
                    .appendGsonBuilder(gson -> gson.setFieldNamingStrategy(FieldNamingPolicy.IDENTITY))
                    .build())
            .build();

    @SerialEntry
    public BannerWriter bannerWriter = new BannerWriter();

    public static class BannerWriter {
        public boolean enabled = true;

        public int permissionlevel = 0;
    }

    @SerialEntry
    public CampfireTimers campfireTimers = new CampfireTimers();

    public static class CampfireTimers {
        public boolean enabled = true;
    }

    @SerialEntry
    public ItemEditor itemEditor = new ItemEditor();

    public static class ItemEditor {
        public boolean enabled = true;

        public boolean nonOpsCanUseCosmeticMode = true;
    }

    @SerialEntry
    public ItemNudging itemNudging = new ItemNudging();

    public static class ItemNudging {
        public boolean shiftUp = true;

        public boolean shiftTowardsPlayer = true;
    }

    @SerialEntry
    public PortableCrafting portableCrafting = new PortableCrafting();

    public static class PortableCrafting {
        public boolean enabled = true;

        public boolean requiresSneak = false;

        public String itemIdOrTag = "#jsst:portable_crafting";
    }
}
