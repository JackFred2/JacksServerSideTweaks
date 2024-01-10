package red.jackf.jsst.util.sgui.labels.data;

import blue.endless.jankson.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import red.jackf.jsst.JSST;
import red.jackf.jsst.util.sgui.labels.LabelMap;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class LabelDataLoader<T> implements SimpleResourceReloadListener<LabelDataLoader.LoadResult<T>> {
    private static final Logger LOGGER = JSST.getLogger("Label Loader");
    private final LabelMap.Datapack<T> map;

    public LabelDataLoader(LabelMap.Datapack<T> map) {
        this.map = map;
    }

    public static <T> void create(LabelMap.Datapack<T> map) {
        LabelDataLoader<T> loader = new LabelDataLoader<>(map);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(loader);
    }

    private boolean pathPredicate(ResourceLocation resourceLocation) {
        return resourceLocation.getPath().equals(getFolder(this.map.getRegistry().key()) + ".json");
    }

    @Override
    public CompletableFuture<LoadResult<T>> load(
            ResourceManager manager,
            ProfilerFiller profiler,
            Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            ItemStack defaultLabel = null;
            Map<T, ItemStack> labels = new HashMap<>();

            for (Map.Entry<ResourceLocation, List<Resource>> entry : manager.listResourceStacks("labels", this::pathPredicate).entrySet()) {
                if (!entry.getKey().getNamespace().equals(JSST.MODID)) continue;
                for (Resource resource : entry.getValue()) {
                    try (Reader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        LabelFile labelFile = LabelFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, json)).getOrThrow(false, LOGGER::error);
                        if (labelFile.replace().isPresent() && labelFile.replace().get()) {
                            labels.clear();
                            defaultLabel = null;
                        }
                        if (labelFile.defaultLabel().isPresent()) defaultLabel = labelFile.defaultLabel().get();
                        for (Map.Entry<ResourceLocation, ItemStack> label : labelFile.labels().entrySet()) {
                            T element = this.map.getRegistry().get(label.getKey());
                            if (element != null) {
                                labels.put(element, label.getValue());
                            } else {
                                LOGGER.debug("Unknown value for registry {}: {}. Ignoring...", getFolder(this.map.getRegistry().key()), label.getKey());
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.error("Couldn't read label file {} from data pack {}", getFolder(this.map.getRegistry().key()), resource.sourcePackId());
                    }
                }
            }

            return new LoadResult<>(defaultLabel, labels);
        });
    }

    @Override
    public CompletableFuture<Void> apply(
            LoadResult<T> data,
            ResourceManager manager,
            ProfilerFiller profiler,
            Executor executor) {
        return CompletableFuture.runAsync(() -> this.map.reload(data), executor);
    }

    @Override
    public ResourceLocation getFabricId() {
        // jsst:labels/mob_effect
        // jsst:labels/block
        return JSST.id(getFolder(this.map.getRegistry().key()));
    }

    private static String getFolder(ResourceKey<? extends Registry<?>> registryKey) {
        return "labels/" + registryKey.location().getPath();
    }

    public record LoadResult<T>(@Nullable ItemStack defaultLabel, Map<T, ItemStack> labels) {}
}
