package red.jackf.jsst.impl.utils.sgui.labels.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Map;

public record RegistryLabelMapFile(Map<ResourceLocation, ItemStack> labels) {
    static final Codec<ItemStack> POSSIBLY_SIMPLE_ITEMSTACK = JFLCodecs.firstInList(ItemStack.SIMPLE_ITEM_CODEC, ItemStack.CODEC);

    public static final Codec<RegistryLabelMapFile> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, POSSIBLY_SIMPLE_ITEMSTACK).fieldOf("labels").forGetter(RegistryLabelMapFile::labels)
            ).apply(instance, RegistryLabelMapFile::new)
    );
}
