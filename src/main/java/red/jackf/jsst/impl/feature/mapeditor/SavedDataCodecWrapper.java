package red.jackf.jsst.impl.feature.mapeditor;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import red.jackf.jsst.impl.mixinutils.JSSTMapItemSavedDataHelper;
import red.jackf.jsst.mixins.mapeditor.MapItemSavedDataAccessor;

import java.util.Map;
import java.util.stream.Collectors;

//? if >=1.21.5 {
public record SavedDataCodecWrapper(Codec<MapItemSavedData> baseCodec) implements Codec<MapItemSavedData> {
    @Override
    public <T> DataResult<Pair<MapItemSavedData, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Pair<MapItemSavedData, T>> base = baseCodec.decode(ops, input);

        Dynamic<T> dyanmic = new Dynamic<>(ops, input);

        DataResult<Pair<Map<String, MapDecoration>, T>> additional = MapEditorCodecs.MAP_CODEC.decode(dyanmic.get(MapEditSession.KEY).orElseEmptyMap());

        return base.map(pair -> {
            var cast = ((JSSTMapItemSavedDataHelper) pair.getFirst());

            additional.result().ifPresent(decorations -> {
                for (Map.Entry<String, MapDecoration> entry : decorations.getFirst().entrySet()) {
                    cast.jsst$addDecoration(entry.getKey(), entry.getValue());
                }
            });

            return pair;
        });
    }

    @Override
    public <T> DataResult<T> encode(MapItemSavedData input, DynamicOps<T> ops, T prefix) {
        DataResult<T> base = baseCodec.encode(input, ops, prefix);

        DataResult<T> additional = MapEditorCodecs.MAP_CODEC.encodeStart(ops, ((MapItemSavedDataAccessor) input).getDecorations()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(MapEditSession.KEY))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return ops.mergeToMap(base.getOrThrow(), ops.createString(MapEditSession.KEY), additional.getOrThrow());
    }
}
//?}
