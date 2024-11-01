package red.jackf.jsst.mixins.mapeditor;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.impl.feature.mapeditor.MapDecoSerialization;
import red.jackf.jsst.impl.feature.mapeditor.MapEditSession;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adds persistence to custom map icons
 */
@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {

    @Final
    @Shadow
    Map<String, MapDecoration> decorations;

    @Shadow private int trackedDecorationCount;

    @SuppressWarnings("DataFlowIssue")
    @ModifyReturnValue(method = "load", at = @At("TAIL"))
    private static MapItemSavedData loadCustomDecorations(MapItemSavedData data, CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(MapEditSession.KEY, CompoundTag.TAG_COMPOUND)) {
            MapDecoSerialization.MAP_CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(MapEditSession.KEY))
                    .ifSuccess(pair -> {
                        for (Map.Entry<String, MapDecoration> entry : pair.getFirst().entrySet()) {
                            // add raw
                            ((MapItemSavedDataMixin) (Object) data).decorations.put(entry.getKey(), entry.getValue());

                            if (entry.getValue().type().value().trackCount()) {
                                ((MapItemSavedDataMixin) (Object) data).trackedDecorationCount++;
                            }
                        }
                    });
        }

        return data;
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void saveCustomDecorations(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        Map<String, MapDecoration> toSave = ((MapItemSavedDataAccessor) this).getDecorations().entrySet().stream()
                .filter(e -> e.getKey().startsWith(MapEditSession.KEY))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!toSave.isEmpty()) {
            MapDecoSerialization.MAP_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), toSave)
                    .ifSuccess(custom -> tag.put(MapEditSession.KEY, custom));
        }
    }
}
