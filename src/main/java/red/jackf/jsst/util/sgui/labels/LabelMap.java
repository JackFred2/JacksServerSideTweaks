package red.jackf.jsst.util.sgui.labels;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.labels.data.LabelDataLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A conversion from arbitrary objects to ItemStack labels.
 * @param <T> Type of objects to hold labels for.
 */
public interface LabelMap<T> {
    ItemStack getLabel(T key);

    static <T> Datapack<T> createDataManaged(Registry<T> registry, ItemStack defaultLabel) {
        Datapack<T> map = new Datapack<>(registry, effect -> defaultLabel);
        LabelDataLoader.create(map);
        return map;
    }

    class Datapack<T> implements LabelMap<T> {
        private final Registry<T> registry;
        private final Function<T, ItemStack> fallback;
        private ItemStack defaultLabel;
        private final Map<T, ItemStack> labels = new HashMap<>();

        public Datapack(Registry<T> registry, Function<T, ItemStack> fallback) {
            this.registry = registry;
            this.fallback = fallback;
        }

        @Override
        public ItemStack getLabel(T key) {
            return labels.getOrDefault(key, defaultLabel == null ? fallback.apply(key) : defaultLabel);
        }

        public void reload(LabelDataLoader.LoadResult<T> data) {
            this.defaultLabel = data.defaultLabel();
            this.labels.clear();
            this.labels.putAll(data.labels());
        }

        public Registry<T> getRegistry() {
            return registry;
        }
    }

    class Static<T> implements LabelMap<T> {
        private final ItemStack defaultLabel;
        private final Map<T, ItemStack> labels = new HashMap<>();

        public Static(ItemStack defaultLabel) {
            this.defaultLabel = defaultLabel;
        }

        public LabelMap<T> addLabel(T key, ItemStack label) {
            this.labels.put(key, label);
            return this;
        }

        @Override
        public ItemStack getLabel(T key) {
            return this.labels.getOrDefault(key, defaultLabel);
        }
    }
}
