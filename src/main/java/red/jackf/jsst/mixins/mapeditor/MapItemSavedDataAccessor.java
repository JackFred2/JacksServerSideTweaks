package red.jackf.jsst.mixins.mapeditor;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
    @Accessor
    int getTrackedDecorationCount();

    @Accessor
    void setTrackedDecorationCount(int newVal);

    @Accessor
    Map<String, MapDecoration> getDecorations();

    @Invoker
    void invokeSetDecorationsDirty();

    @Invoker
    void invokeRemoveDecoration(String id);

    @Invoker
    void invokeAddDecoration(Holder<MapDecorationType> decorationType, @Nullable LevelAccessor level, String id, double x, double z, double yRot, @Nullable Component displayName);

    @Invoker
    static boolean invokeIsInsideMap(float x, float y) {
        throw new AssertionError("mixin impl");
    }
}
