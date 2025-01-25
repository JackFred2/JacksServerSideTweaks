package red.jackf.jsst.impl.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Version-aware registry methods
 */
public interface RegistryUtils {
    static <T> Registry<T> lookup(RegistryAccess access, ResourceKey<Registry<T>> key) {
        //? if <=1.21.1 {
        /*return access.registryOrThrow(key);
        *///?} else
        return access.lookupOrThrow(key);
    }

    static <T> Stream<Holder<T>> stream(Registry<T> registry) {
        //? if <=1.21.1 {
        /*return registry.holders().map(ref -> ref);
        *///?} else
        return registry.listElements().map(ref -> ref);
    }

    static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, ResourceKey<T> key) {
        //? if <=1.21.1 {
        /*return registry.getHolder(key);
        *///?} else
        return registry.get(key);
    }

    static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, ResourceLocation key) {
        //? if <=1.21.1 {
        /*return registry.getHolder(key);
        *///?} else
        return registry.get(key);
    }

    static <T> HolderSet<T> getValuesFromIDOrTag(RegistryAccess registries, ResourceKey<Registry<T>> registryKey, String idOrTag) {
        if (!TextUtils.isValidReslocOrTag(idOrTag)) return HolderSet.empty();

        Registry<T> registry = lookup(registries, registryKey);

        if (idOrTag.startsWith("#")) {
            //? if <=1.21.1 {
            /*Optional<HolderSet.Named<T>> set = registry.getTag(TagKey.create(registryKey, ResourceLocation.parse(idOrTag.substring(1))));
            *///?} else
            Optional<HolderSet.Named<T>> set = registry.get(TagKey.create(registryKey, ResourceLocation.parse(idOrTag.substring(1))));
            if (set.isPresent()) return set.get();
            else return HolderSet.empty();
        } else {
            //? if <=1.21.1 {
            /*Optional<Holder.Reference<T>> item = registry.getHolder(ResourceKey.create(registryKey, ResourceLocation.parse(idOrTag)));
            *///?} else
            Optional<Holder.Reference<T>> item = registry.get(ResourceKey.create(registryKey, ResourceLocation.parse(idOrTag)));
            if (item.isPresent()) return HolderSet.direct(item.get());
            else return HolderSet.empty();
        }
    }

    static <T> Optional<TagKey<T>> parseTag(ResourceKey<Registry<T>> registryKey, String possibleTag) {
        return ResourceLocation.read(possibleTag).result().map(resLoc -> TagKey.create(registryKey, resLoc));
    }

    static <T> Stream<Holder<T>> streamTag(Registry<T> registry, TagKey<T> key) {
        //? if <=1.21.1 {
        /*var tag = registry.getTag(key);
         *///?} else
        var tag = registry.get(key);

        return tag.map(HolderSet.ListBacked::stream).orElseGet(Stream::empty);
    }
}
