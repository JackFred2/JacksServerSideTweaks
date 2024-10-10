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
    public PortableCrafting portableCrafting = new PortableCrafting();

    public static class PortableCrafting {
        public boolean enabled = true;

        public boolean requiresSneak = false;

        public String itemIdOrTag = "#jsst:portable_crafting";
    }
}
