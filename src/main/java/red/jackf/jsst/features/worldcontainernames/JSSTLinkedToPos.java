package red.jackf.jsst.features.worldcontainernames;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface JSSTLinkedToPos {
    void setLinked(BlockPos pos);

    @Nullable
    BlockPos getLinked();
}
