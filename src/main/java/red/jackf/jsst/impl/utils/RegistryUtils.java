package red.jackf.jsst.impl.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public interface RegistryUtils {
    static <T> Registry<T> lookup(RegistryAccess access, ResourceKey<Registry<T>> key) {
        //? if <=1.21.1 {
        /*return access.registryOrThrow(key);
        *///?} else
        return access.lookupOrThrow(key);
    }

    static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, ResourceKey<T> key) {
        //? if <=1.21.1 {
        /*return registry.getHolder(key);
        *///?} else
        return registry.get(key);
    }
}
