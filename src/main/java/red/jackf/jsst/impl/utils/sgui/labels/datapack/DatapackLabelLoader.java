package red.jackf.jsst.impl.utils.sgui.labels.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
//? if <=1.21.1
/*import net.minecraft.util.profiling.ProfilerFiller;*/
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import red.jackf.jsst.impl.JSST;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DatapackLabelLoader<T> implements SimpleResourceReloadListener<DatapackLabelLoader.LoadResult<T>> {
    private static final Logger LOGGER = JSST.getLogger("Label Loader");

    private final ResourceLocation id;
    private final ResourceKey<Registry<T>> registryKey;
    private final HolderLookup.Provider provider;
    private final DatapackLabelMap<T> map;

    public DatapackLabelLoader(ResourceLocation id, ResourceKey<Registry<T>> registryKey, HolderLookup.Provider provider, DatapackLabelMap<T> map) {
        this.id = id;
        this.registryKey = registryKey;
        this.provider = provider;
        this.map = map;
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.id;
    }

    private String getPath() {
        return "labels/" + this.registryKey.location().getPath() + ".json";
    }

    private boolean pathPredicate(ResourceLocation location) {
        return location.getNamespace().equals(JSST.MOD_ID) && location.getPath().equals(getPath());
    }

    @Override
    public CompletableFuture<LoadResult<T>> load(ResourceManager manager, /*? if <=1.21.1 {*//*ProfilerFiller ignored,*//*?}*/ Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Holder<T>, ItemStack> labels = new HashMap<>();

            for (Map.Entry<ResourceLocation, List<Resource>> entries : manager.listResourceStacks("labels", this::pathPredicate).entrySet()) {
                for (Resource resource : entries.getValue()) {
                    try (Reader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        DatapackLabelMapFile file = DatapackLabelMapFile.CODEC.parse(this.provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();

                        for (Map.Entry<ResourceLocation, ItemStack> labelEntry : file.labels().entrySet()) {
                            ResourceKey<T> key = ResourceKey.create(this.registryKey, labelEntry.getKey());

                            Optional<Holder.Reference<T>> element = this.provider.lookupOrThrow(this.registryKey).get(key);

                            if (element.isPresent()) {
                                labels.put(element.get(), labelEntry.getValue());
                            } else if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                                LOGGER.warn("Unknown key {}; this may be a compatibility entry and safe to ignore.", key);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Couldn't read label file {} from datapack '{}'", getPath(), resource.sourcePackId(), e);
                    }
                }
            }

            return new LoadResult<>(labels);
        });
    }

    @Override
    public CompletableFuture<Void> apply(LoadResult<T> data, ResourceManager manager, /*? if <=1.21.1 {*//*ProfilerFiller ignored,*//*?}*/ Executor executor) {
        return CompletableFuture.runAsync(() -> this.map.reload(data), executor);
    }

    public record LoadResult<T>(Map<Holder<T>, ItemStack> labelMap) {}
}
