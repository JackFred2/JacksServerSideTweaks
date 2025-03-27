package red.jackf.jsst.mixins.mapeditor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if <=1.21.4 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
*///?}
import com.mojang.serialization.Codec;
//? if <=1.21.4 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
*///?}
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
//? if <=1.21.4 {
/*import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.mapeditor.MapEditorCodecs;
import red.jackf.jsst.impl.feature.mapeditor.MapEditSession;
*///?}
//? if >=1.21.5
import red.jackf.jsst.impl.feature.mapeditor.SavedDataCodecWrapper;
import red.jackf.jsst.impl.mixinutils.JSSTMapItemSavedDataHelper;

import java.util.Map;
//? if <=1.21.4
/*import java.util.stream.Collectors;*/

/**
 * Adds persistence to custom map icons
 */
@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements JSSTMapItemSavedDataHelper {

    @Final
    @Shadow
    Map<String, MapDecoration> decorations;

    @Shadow private int trackedDecorationCount;

    //? if >=1.21.5 {
    @ModifyExpressionValue(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"),
            remap = false)
    private static Codec<MapItemSavedData> wrapCodec(Codec<MapItemSavedData> original) {
        return new SavedDataCodecWrapper(original);
    }
    //?}

    //? if <=1.21.4 {
    /*@SuppressWarnings("DataFlowIssue")
    @ModifyReturnValue(method = "load", at = @At("TAIL"))
    private static MapItemSavedData loadCustomDecorations(MapItemSavedData data, CompoundTag tag, HolderLookup.Provider registries) {
        if (!JSSTConfig.INSTANCE.instance().mapEditor.disableSerialization && tag.contains(MapEditSession.KEY, CompoundTag.TAG_COMPOUND)) {
            MapEditorCodecs.MAP_CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(MapEditSession.KEY))
                    .ifSuccess(pair -> {
                        for (Map.Entry<String, MapDecoration> entry : pair.getFirst().entrySet()) {
                            // add raw
                            ((MapItemSavedDataMixin) (Object) data).jsst$addDecoration(entry.getKey(), entry.getValue());
                        }
                    });
        }

        return data;
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void saveCustomDecorations(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        if (JSSTConfig.INSTANCE.instance().mapEditor.disableSerialization) return;

        Map<String, MapDecoration> toSave = ((MapItemSavedDataAccessor) this).getDecorations().entrySet().stream()
                .filter(e -> e.getKey().startsWith(MapEditSession.KEY))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!toSave.isEmpty()) {
            MapEditorCodecs.MAP_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), toSave)
                    .ifSuccess(custom -> tag.put(MapEditSession.KEY, custom));
        }
    }
    *///?}

    @Override
    public void jsst$addDecoration(String key, MapDecoration decoration) {
        this.decorations.put(key, decoration);

        if (decoration.type().value().trackCount()) {
            this.trackedDecorationCount++;
        }
    }
}
