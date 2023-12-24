package red.jackf.jsst.util.sgui.labels;

import blue.endless.jankson.annotation.Nullable;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.sgui.Styles;
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

    default LabelMap<T> withAdditional(Map<T, ItemStack> additional) {
        return new Wrapped<>(this, additional);
    }

    static <T> Datapack<T> createDataManaged(Registry<T> registry, Function<T, ItemStack> defaultLabel, Function<T, Component> nameGetter) {
        Datapack<T> map = new Datapack<>(registry, defaultLabel, nameGetter);
        LabelDataLoader.create(map);
        return map;
    }

    class Datapack<T> implements LabelMap<T> {
        private final Registry<T> registry;
        private final Function<T, ItemStack> fallback;
        private final Function<T, Component> nameGetter;
        private ItemStack defaultLabel;
        private final Map<T, ItemStack> labels = new HashMap<>();

        public Datapack(Registry<T> registry, Function<T, ItemStack> fallback, Function<T, Component> nameGetter) {
            this.registry = registry;
            this.fallback = fallback;
            this.nameGetter = nameGetter;
        }

        @Override
        public ItemStack getLabel(@Nullable T key) {
            ItemStack base = labels.getOrDefault(key, defaultLabel == null ? fallback.apply(key) : defaultLabel);
            return GuiElementBuilder.from(base)
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

    record Wrapped<T>(LabelMap<T> base, Map<T, ItemStack> additional) implements LabelMap<T> {
        @Override
        public ItemStack getLabel(T key) {
            ItemStack additional = additional().get(key);
            if (additional != null) return additional;
            else return base.getLabel(key);
        }
    }
}
