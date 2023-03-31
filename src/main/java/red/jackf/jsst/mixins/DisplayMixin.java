package red.jackf.jsst.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.jsst.features.worldcontainernames.JSSTLinkedToPos;

/**
 * Used by:
 * World Container Names - to link a display with a position; fixes some bugs with chunk unload/reloads
 */
@Mixin(Display.class)
public class DisplayMixin implements JSSTLinkedToPos {
    @Unique
    private BlockPos linked = null;

    @Unique
    private static final String LINKED_KEY = "jsst_linked_to";

    @Override
    public void jsst_setLinked(BlockPos pos) {
        this.linked = pos;
    }

    @Override
    @Nullable
    public BlockPos jsst_getLinked() {
        return this.linked;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLinkedLocation(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(LINKED_KEY)) this.linked = BlockPos.of(tag.getLong(LINKED_KEY));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addLinkedLocation(CompoundTag tag, CallbackInfo ci) {
        if (this.linked != null) tag.putLong(LINKED_KEY, linked.asLong());
    }
}
