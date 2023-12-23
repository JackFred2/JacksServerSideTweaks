package red.jackf.jsst.util.sgui.labels.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.util.JSSTCodecs;

import java.util.Map;
import java.util.Optional;

public record LabelFile(Map<ResourceLocation, ItemStack> labels, Optional<ItemStack> defaultLabel, Optional<Boolean> replace) {
    public static final Codec<LabelFile> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, JSSTCodecs.SIMPLIFIED_ITEMSTACK).fieldOf("labels").forGetter(LabelFile::labels),
                    JSSTCodecs.SIMPLIFIED_ITEMSTACK.optionalFieldOf("default").forGetter(LabelFile::defaultLabel),
                    Codec.BOOL.optionalFieldOf("replace").forGetter(LabelFile::replace)
            ).apply(instance, LabelFile::new));
}
