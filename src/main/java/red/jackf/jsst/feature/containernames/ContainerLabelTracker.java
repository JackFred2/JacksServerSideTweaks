package red.jackf.jsst.feature.containernames;

import net.minecraft.core.BlockPos;

import java.util.Map;

public interface ContainerLabelTracker {
    Map<BlockPos, LabelLie> jsst$containernames$getLabels();
}
