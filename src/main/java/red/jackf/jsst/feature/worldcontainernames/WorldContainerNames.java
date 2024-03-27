package red.jackf.jsst.feature.worldcontainernames;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import red.jackf.jackfredlib.api.base.ServerTracker;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;
import red.jackf.jsst.mixins.containernames.ChunkMapAccessor;
import red.jackf.jsst.util.Scheduler;

import java.util.*;

public class WorldContainerNames extends ToggleFeature<WorldContainerNames.Config> {
    public static final WorldContainerNames INSTANCE = new WorldContainerNames();
    private static final Logger LOGGER = JSST.getLogger("World Container Names");

    private WorldContainerNames() {}

    @Override
    public void setup() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (be instanceof BaseContainerBlockEntity cbe && config().enabled) {
                // wait a tick for BE data to load
                Scheduler.INSTANCE.scheduleNextTick(level, level1 -> updateLabel(level1, cbe, false));
            }
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> {
            if (be instanceof BaseContainerBlockEntity cbe && config().enabled) {
                updateLabel(level, cbe, true);
            }
        });
    }

    /**
     * Get the label list for a level
     */
    private static Map<BlockPos, LabelLie> getLabels(ServerLevel level) {
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
        // remove old lie at position(s)
        List<BlockPos> connected = gatherConnected(cbe.getBlockState(), cbe.getBlockPos());

        for (BlockPos pos : connected) {
            var existing = getLabels(level).remove(pos);
            if (existing != null) existing.fade();
        }

        if (isRemoval) connected.remove(cbe.getBlockPos());

        if (connected.isEmpty()) return;

        // change the position and block entity to work with if needed (double chests)
        BlockPos basePosition = connected.get(0);
        Optional<BaseContainerBlockEntity> baseBlockEntity = connected.stream()
                                                                      .filter(level::isLoaded) // don't re-load chunks when the level is unloaded
                                                                      .map(level::getBlockEntity)
                                                                      .filter(be -> be instanceof BaseContainerBlockEntity cbe2 && cbe2.hasCustomName())
                                                                      .map(be -> (BaseContainerBlockEntity) be)
                                                                      .findFirst();

        if (baseBlockEntity.isEmpty()) return;
        cbe = baseBlockEntity.get();

        // create label
        Vec3 position = getLabelPositionFor(connected);
        Display labelEntity = EntityBuilders.textDisplay(level)
                                                        .text(cbe.getCustomName())
                                                        .position(position)
                                                        .billboard(Display.BillboardConstraints.CENTER)
                                                        .viewRangeModifier((float) (config().viewRange / 64))
                                                        .build();

        EntityLie<Display> lie = EntityLie.builder(labelEntity)
                                                      .createAndShow();

        Tracker<EntityLie<? extends Display>> tracker = Tracker.<EntityLie<? extends Display>>builder(level)
                                                               .addLie(lie)
                                                               .setFocus(position, 6 * config().viewRange) // just over the limit for client modifier
                                                               .setUpdateInterval(5 * SharedConstants.TICKS_PER_SECOND)
                                                               .build(true);

        getLabels(level).put(basePosition, new LabelLie(tracker, position));
    }

    private static List<ServerLevel> getLoadedLevels() {
        MinecraftServer server = ServerTracker.INSTANCE.getServer();
        if (server == null) return Collections.emptyList();
        List<ServerLevel> levels = new ArrayList<>();
        server.getAllLevels().forEach(levels::add);
        return levels;
    }

    @Override
    public void disable() {
        for (ServerLevel level : getLoadedLevels()) {
            getLabels(level).values().forEach(LabelLie::fade);
            getLabels(level).clear();
        }
    }

    @Override
    public void enable() {
        var levels = getLoadedLevels();
        if (levels.isEmpty()) return;
        LOGGER.debug("Adding labels to all loaded block entities; server may stutter for a bit.");
        for (ServerLevel level : levels) {
            Scheduler.INSTANCE.scheduleNextTick(level, level1 -> {
                for (ChunkHolder chunk : ((ChunkMapAccessor) level1.getChunkSource().chunkMap).jsst$containernames$getChunks()) {
                    var ticking = chunk.getTickingChunk();
                    if (ticking == null) continue;
                    for (BlockEntity be : ticking.getBlockEntities().values()) {
                        if (be instanceof BaseContainerBlockEntity cbe) updateLabel(level1, cbe, false);
                    }
                }
            });
        }
    }

    @Override
    public void reload(Config current) {
        super.reload(current);
        if (current.enabled) {
            for (ServerLevel level : getLoadedLevels()) {
                for (LabelLie label : getLabels(level).values()) {
                    label.tracker().setFocus(label.position(), 6 * config().viewRange);

                    for (EntityLie<? extends Display> lie : label.tracker().getManagedLies()) {
                        EntityUtils.setDisplayViewRange(lie.entity(), (float) (current.viewRange / 64));
                    }
                }
            }
        }
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
