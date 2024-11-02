package red.jackf.jsst.impl.feature.mapeditor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.Map;

public interface MapEditorCodecs {
    Codec<MapDecoration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.MAP_DECORATION_TYPE.holderByNameCodec().fieldOf("type").forGetter(MapDecoration::type),
            Codec.BYTE.fieldOf("x").forGetter(MapDecoration::x),
            Codec.BYTE.fieldOf("y").forGetter(MapDecoration::y),
            Codec.BYTE.fieldOf("rot").forGetter(MapDecoration::rot),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(MapDecoration::name)
    ).apply(instance, MapDecoration::new));

    Codec<Map<String, MapDecoration>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
}
