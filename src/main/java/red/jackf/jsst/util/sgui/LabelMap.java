package red.jackf.jsst.util.sgui;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * A conversion from arbitrary objects to ItemStack labels.
 * @param <T> Type of objects to hold labels for.
 */
public class LabelMap<T> {
    private final Map<T, ItemStack> icons = new HashMap<>();
    private final ItemStack defaultLabel;

    public LabelMap(ItemStack def) {
        this.defaultLabel = def;
    }

    public LabelMap<T> addMapping(T key, ItemStack label) {
        this.icons.put(key, label);
        return this;
    }

    public ItemStack getLabel(T key) {
        return this.icons.getOrDefault(key, defaultLabel);
    }
}
