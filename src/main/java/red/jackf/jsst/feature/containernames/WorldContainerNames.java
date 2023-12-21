package red.jackf.jsst.feature.containernames;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;
import red.jackf.jsst.util.Scheduler;

import java.util.*;

public class WorldContainerNames extends ToggleFeature<WorldContainerNames.Config> {
    public static final WorldContainerNames INSTANCE = new WorldContainerNames();

    private WorldContainerNames() {}

    @Override
    public void setup() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (be instanceof BaseContainerBlockEntity cbe) {
                // wait a tick for BE data to load
                Scheduler.INSTANCE.scheduleNextTick(level, level1 -> updateLabel(level1, cbe, false));
            }
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> {
            if (be instanceof BaseContainerBlockEntity cbe) {
                updateLabel(level, cbe, true);
            }
        });
    }

    /**
     * Get the label list for a level
     */
    private static Map<BlockPos, EntityLie<? extends Display>> getLabels(ServerLevel level) {
        return ((ContainerLabelTracker) level).jsst$containernames$getLabels();
    }

    /**
     * Get a list of all block positions connected to the given position, including itself. Sorted in a deterministic way.
     */
    private static List<BlockPos> gatherConnected(BlockState state, BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(pos);

        if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            positions.add(pos.relative(ChestBlock.getConnectedDirection(state)));
        }

        positions.sort(Comparator.comparingLong(BlockPos::asLong));

        return positions;
    }

    /**
     * Gets centered position for a group of BlockPos
     */
    private static Vec3 getLabelPositionFor(Collection<BlockPos> positions) {
        double x = 0;
        double y = 0;
        double z = 0;

        for (BlockPos position : positions) {
            x += position.getX() + 0.5;
            y += position.getY() + 1.4;
            z += position.getZ() + 0.5;
        }

        return new Vec3(x / positions.size(), y / positions.size(), z / positions.size());
    }

    private void updateLabel(ServerLevel level, BaseContainerBlockEntity cbe, boolean isRemoval) {
        List<BlockPos> connected = gatherConnected(cbe.getBlockState(), cbe.getBlockPos());

        for (BlockPos pos : connected) {
            var existing = getLabels(level).remove(pos);
            if (existing != null) existing.fade();
        }

        if (isRemoval) connected.remove(cbe.getBlockPos());

        if (connected.isEmpty()) return;

        BlockPos basePosition = connected.get(0);
        Optional<BaseContainerBlockEntity> baseBlockEntity = connected.stream()
                                                                      .filter(level::isLoaded) // don't re-load chunks when the level is unloaded
                                                                      .map(level::getBlockEntity)
                                                                      .filter(be -> be instanceof BaseContainerBlockEntity cbe2 && cbe2.hasCustomName())
                                                                      .map(be -> (BaseContainerBlockEntity) be)
                                                                      .findFirst();

        if (baseBlockEntity.isEmpty()) return;

        cbe = baseBlockEntity.get();

        Vec3 position = getLabelPositionFor(connected);

        Display.TextDisplay labelEntity = EntityBuilders.textDisplay(level)
                                                        .text(cbe.getCustomName())
                                                        .position(position)
                                                        .billboard(Display.BillboardConstraints.CENTER)
                                                        .viewRangeModifier(8f / 64)
                                                        .build();

        EntityLie<Display.TextDisplay> lie = EntityLie.builder(labelEntity)
                                                      .createAndShow();

        getLabels(level).put(basePosition, lie);

        Tracker.<EntityLie<Display.TextDisplay>>builder(level)
               .addLie(lie)
               .setFocus(position, 6 * config().viewRange)
               .setUpdateInterval(5 * SharedConstants.TICKS_PER_SECOND)
               .build(true);
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().worldContainerNames;
    }

    public static class Config extends ToggleFeature.Config {
        @Comment("""
                How far away a player has to be from a container to view the name. This is modified by the player's entity view distance modifier.
                Options: [4, 16] blocks
                Default: 8 blocks""")
        public double viewRange = 8;

        public void validate() {
            this.viewRange = Mth.clamp(this.viewRange, 4, 16);
        }
    }
}
