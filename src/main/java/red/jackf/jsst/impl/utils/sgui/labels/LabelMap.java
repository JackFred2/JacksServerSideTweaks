package red.jackf.jsst.impl.utils.sgui.labels;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.sgui.labels.registry.RegistryLabelMap;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface LabelMap<T> extends Function<T, ItemStack> {
    static <T> LabelMap<Holder<T>> createForRegistry(ResourceKey<Registry<T>> registryKey, Function<Holder<T>, ItemStack> defaultFunction, BiFunction<Holder<T>, ItemStack, ItemStack> posProcess) {
        return RegistryLabelMap.create(registryKey, defaultFunction, posProcess);
    }

    ItemStack apply(T option);
}
