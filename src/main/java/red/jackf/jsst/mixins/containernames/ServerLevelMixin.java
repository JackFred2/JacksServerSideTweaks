package red.jackf.jsst.mixins.containernames;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jsst.feature.containernames.ContainerLabelTracker;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ContainerLabelTracker {
    @Unique
    private final Map<BlockPos, EntityLie<? extends Display>> labels = new HashMap<>();

    @Override
    public Map<BlockPos, EntityLie<? extends Display>> jsst$containernames$getLabels() {
        return labels;
    }
}
