package red.jackf.jsst.features.commanddefineddatapack;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import red.jackf.jsst.command.CommandUtils;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PackState {
    private static final String FOLDER_NAME = "jsstCDD";
    private static final String PACK_MCMETA_TEMPLATE = "/pack.mcmeta.template";

    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, TagFile>> tags = new HashMap<>();
    private final MinecraftServer server;

    public PackState(MinecraftServer server) {
        this.server = server;
    }

    private static ResourceLocation shorten(ResourceLocation source, String toCut) {
        return source.withPath(s -> s.replace(toCut + "/", "").replace(".json", ""));
    }

    public static PackState load(MinecraftServer server)  {
        var pack = server.getPackRepository().getPack("file/" + FOLDER_NAME);
        var state = new PackState(server);
        if (pack != null) try (var resources = pack.open()) {
            AtomicInteger errorCount = new AtomicInteger();

            for (var namespace : resources.getNamespaces(PackType.SERVER_DATA)) {

                // tags
                BuiltInRegistries.REGISTRY.entrySet().forEach(registryEntry -> {
                    var tagDir = TagManager.getTagDir(registryEntry.getKey());
                    resources.listResources(PackType.SERVER_DATA, namespace, tagDir, (resLoc, stream) -> {
                        try {
                            var json = JsonParser.parseReader(new InputStreamReader(stream.get()));
                            var resultImmutable = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, json)).getOrThrow(false, s -> CommandDefinedDatapack.LOGGER.error("Error loading " + resLoc + ": " + s));
                            var result = new TagFile(new ArrayList<>(resultImmutable.entries()), resultImmutable.replace());
                            state.tags.computeIfAbsent(registryEntry.getKey(), k -> new HashMap<>()).put(shorten(resLoc, tagDir), result);
                        } catch (IOException | RuntimeException e) {
                            errorCount.getAndIncrement();
                        }
                    });
                });
            }

            if (errorCount.get() > 0) {
                CommandDefinedDatapack.LOGGER.error(errorCount.get() + " errors while loading file/" + FOLDER_NAME + " datapack!");
            }
        }

        return state;
    }

    protected Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, TagFile>> getTags() {
        return tags;
    }

    private static void copyPackMcmeta(Path dir) throws IOException {
        try (var fileStream = PackState.class.getResourceAsStream(PACK_MCMETA_TEMPLATE)) {
            if (fileStream == null) {
                CommandDefinedDatapack.LOGGER.error("Could not find pack.mcmeta.template");
                return;
            }
            CommandDefinedDatapack.LOGGER.info("Copying mcmeta");
            try (var writer = new FileOutputStream(dir.resolve("pack.mcmeta").toFile())) {
                IOUtils.copy(fileStream, writer);
            }
        }
    }

    public boolean save() throws CommandRuntimeException {
        try {
            var rootDir = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(FOLDER_NAME);
            if (!Files.exists(rootDir)) {
                Files.createDirectory(rootDir);
                copyPackMcmeta(rootDir);
            }
            var dataDir = rootDir.resolve("data");
            FileUtils.cleanDirectory(dataDir.toFile());
            FileUtils.writeStringToFile(dataDir.resolve("editing.warning").toFile(), "This pack is deleted and remade every save; any files not supported may be deleted without warning.", "UTF-8");
            for (var registry : tags.entrySet())
                for (var tagEntry : registry.getValue().entrySet()) {
                    CommandDefinedDatapack.LOGGER.info(tagEntry.toString());
                    var path = dataDir.resolve(tagEntry.getKey().getNamespace())
                            .resolve(TagManager.getTagDir(registry.getKey()))
                            .resolve(tagEntry.getKey().getPath() + ".json");
                    Files.createDirectories(path.getParent());
                    var json = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, tagEntry.getValue())
                            .getOrThrow(false, CommandDefinedDatapack.LOGGER::error);
                    var writer = new JsonWriter(new FileWriter(path.toFile()));
                    writer.setIndent("  ");
                    writer.setSerializeNulls(false);
                    GsonHelper.writeValue(writer, json, DataProvider.KEY_COMPARATOR);
                    writer.close();
                    CommandDefinedDatapack.LOGGER.info(json.toString());
                }

            CommandDefinedDatapack.LOGGER.info("Saved new Datapack.");
            return true;
        } catch (IOException e) {
            CommandDefinedDatapack.LOGGER.error("Error saving new datapack", e);
            throw new CommandRuntimeException(CommandUtils.errorPrefix().append(CommandUtils.text("Error saving datapack, please check console.")));
        }
    }
}
