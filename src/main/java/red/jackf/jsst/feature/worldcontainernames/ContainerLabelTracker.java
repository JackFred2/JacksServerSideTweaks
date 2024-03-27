package red.jackf.jsst.feature.worldcontainernames;

import net.minecraft.core.BlockPos;

import java.util.Map;

public interface ContainerLabelTracker {
    Map<BlockPos, LabelLie> jsst$containernames$getLabels();
}
