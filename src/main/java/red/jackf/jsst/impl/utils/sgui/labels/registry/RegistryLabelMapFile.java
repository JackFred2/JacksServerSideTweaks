package red.jackf.jsst.impl.utils.sgui.labels.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.impl.utils.ModCodecs;

import java.util.Map;

public record RegistryLabelMapFile(Map<ResourceLocation, ItemStack> labels) {
    public static final Codec<RegistryLabelMapFile> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, ModCodecs.POSSIBLY_SIMPLE_STACK).fieldOf("labels").forGetter(RegistryLabelMapFile::labels)
            ).apply(instance, RegistryLabelMapFile::new)
    );
}
