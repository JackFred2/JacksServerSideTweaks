package red.jackf.jsst.util.sgui.labels;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.labels.data.LabelDataLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A conversion from arbitrary objects to ItemStack labels.
 * @param <T> Type of objects to hold labels for.
 */
public interface LabelMap<T> {
    ItemStack getLabel(T key);

    default LabelMap<T> withAdditional(BiFunction<T, LabelMap<T>, ItemStack> additional) {
        return new Wrapped<>(this, additional);
    }

    default LabelMap<T> withAdditional(Map<T, ItemStack> additional) {
        return new Wrapped<>(this, (item, map) -> additional.get(item));
    }

    static <T> Datapack<T> createDataManaged(Registry<T> registry, Function<T, ItemStack> defaultLabel, Function<T, Component> nameGetter) {
        Datapack<T> map = new Datapack<>(registry, defaultLabel, nameGetter);
        LabelDataLoader.create(map);
        return map;
    }

    static <T> LabelMap<T> createStatic(Function<T, ItemStack> defaultGetter) {
        return createStatic(Collections.emptyMap(), defaultGetter);
    }

    static <T> LabelMap<T> createStatic(Map<T, ItemStack> labels, Function<T, ItemStack> defaultGetter) {
        Static<T> map = new Static<>(defaultGetter);
        labels.forEach(map::addLabel);
        return map;
    }

    static <T> LabelMap<T> createStatic(Map<T, ItemStack> labels) {
        return createStatic(labels, t -> {
            throw new IllegalArgumentException("Unknown key");
        });
    }

    class Datapack<T> implements LabelMap<T> {
        private final Registry<T> registry;
        private final Function<T, ItemStack> fallback;
        private final Function<T, Component> nameGetter;
        private ItemStack defaultLabel;
        private final Map<T, ItemStack> labels = new HashMap<>();

        private Datapack(Registry<T> registry, Function<T, ItemStack> fallback, Function<T, Component> nameGetter) {
            this.registry = registry;
            this.fallback = fallback;
            this.nameGetter = nameGetter;
        }

        @Override
        public ItemStack getLabel(@Nullable T key) {
            ItemStack base = labels.getOrDefault(key, defaultLabel == null ? fallback.apply(key) : defaultLabel);
            return JSSTElementBuilder.from(base)
                    .setName(nameGetter.apply(key))
                    .addLoreLine(Component.literal(String.valueOf(this.registry.getKey(key))).setStyle(Styles.ID))
                    .hideFlags()
                    .asStack();
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
        private final Function<T, ItemStack> defaultLabel;
        private final Map<T, ItemStack> labels = new HashMap<>();

        private Static(Function<T, ItemStack> defaultGetter) {
            this.defaultLabel = defaultGetter;
        }

        public LabelMap<T> addLabel(T key, ItemStack label) {
            this.labels.put(key, label);
            return this;
        }

        @Override
        public ItemStack getLabel(T key) {
            var result = this.labels.get(key);
            if (result != null) return result;
            else return defaultLabel.apply(key);
        }
    }

    record Wrapped<T>(LabelMap<T> base, BiFunction<T, LabelMap<T>, ItemStack> additional) implements LabelMap<T> {
        @Override
        public ItemStack getLabel(T key) {
            ItemStack additional = additional().apply(key, base);
            if (additional != null) return additional;
            else return base.getLabel(key);
        }
    }
}
