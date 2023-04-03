package red.jackf.jsst.features.commanddefineddatapack;

import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagManager;
import red.jackf.jsst.JSST;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PackState {
    private static final String FOLDER_NAME = "jsstCDD";

    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, TagFile>> tags = new HashMap<>();

    private static ResourceLocation shorten(ResourceLocation source, String toCut) {
        return source.withPath(s -> s.replace(toCut + "/", "").replace(".json", ""));
    }

    public static PackState load(MinecraftServer server)  {
        var pack = server.getPackRepository().getPack("file/" + FOLDER_NAME);
        var state = new PackState();
        if (pack != null) try (var resources = pack.open()) {
            for (var namespace : resources.getNamespaces(PackType.SERVER_DATA)) {
                BuiltInRegistries.REGISTRY.entrySet().forEach(registryEntry -> {
                    var tagDir = TagManager.getTagDir(registryEntry.getKey());
                    resources.listResources(PackType.SERVER_DATA, namespace, tagDir, (resLoc, stream) -> {
                        try {
                            var json = JsonParser.parseReader(new InputStreamReader(stream.get()));
                            var result = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, json)).getOrThrow(false, JSST.LOGGER::error);
                            state.tags.computeIfAbsent(registryEntry.getKey(), k -> new HashMap<>()).put(shorten(resLoc, tagDir), result);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
            }
        }

        JSST.LOGGER.info(state.tags.toString());

        return state;
    }
}
