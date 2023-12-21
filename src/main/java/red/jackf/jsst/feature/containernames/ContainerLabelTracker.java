package red.jackf.jsst.feature.containernames;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

import java.util.Map;

public interface ContainerLabelTracker {
    Map<BlockPos, EntityLie<? extends Display>> jsst$containernames$getLabels();
}
