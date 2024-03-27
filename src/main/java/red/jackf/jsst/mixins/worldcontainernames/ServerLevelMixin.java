package red.jackf.jsst.mixins.worldcontainernames;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import red.jackf.jsst.feature.worldcontainernames.ContainerLabelTracker;
import red.jackf.jsst.feature.worldcontainernames.LabelLie;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ContainerLabelTracker {
    @Unique
    private final Map<BlockPos, LabelLie> labels = new HashMap<>();

    @Override
    public Map<BlockPos, LabelLie> jsst$containernames$getLabels() {
        return labels;
    }
}
