package red.jackf.jsst.features.worldcontainernames;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Triple;
import red.jackf.jsst.JSST;
import red.jackf.jsst.features.Feature;

public class WorldContainerNames extends Feature<WorldContainerNames.Config> {
    private static final String JSST_TAG = "jsst_world_container_name";

    private final BiMap<BlockEntity, Display.TextDisplay> displayCache = HashBiMap.create();

    private final Multimap<Long, Triple<BlockPos, ServerLevel, Boolean>> delayedChecks = HashMultimap.create();

    // Creates or updates a text entity
    private void createOrUpdateText(BlockEntity be, ServerLevel level) {
        var textDisplay = displayCache.get(be);
        var result = DisplayParser.parse(be);
        if (result != null) {
            if (textDisplay == null) {
                textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
                textDisplay.addTag(JSST_TAG);
                textDisplay.setViewRange(0.2f);
                textDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                displayCache.put(be, textDisplay);
                ((JSSTLinkedToPos) textDisplay).setLinked(be.getBlockPos());
                level.addFreshEntity(textDisplay);
            }
            textDisplay.setPos(result.pos());
            textDisplay.setText(result.text());
        } else if (textDisplay != null) {
            displayCache.remove(be);
            textDisplay.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    // Remove a text, updating linked
    private void removeText(BlockEntity be, ServerLevel level) {
        level.getProfiler().push("jsst_world_containers");
        var toUpdate = UpdateParser.parse(be);
        var display = displayCache.remove(be);
        if (display != null)
            display.remove(Entity.RemovalReason.DISCARDED);
        for (BlockPos otherPos : toUpdate)
            delayedChecks.put(level.getGameTime() + 1, Triple.of(otherPos, level, false));
        level.getProfiler().pop();
    }

    private void checkBlockEntity(BlockPos pos, ServerLevel level, Boolean propagate) {
        var be = level.getBlockEntity(pos);
        if (be != null && !be.isRemoved()) {
            var toUpdate = propagate ? UpdateParser.parse(be) : null;
            createOrUpdateText(be, level);
            if (propagate) {
                for (BlockPos otherPos : toUpdate)
                    checkBlockEntity(otherPos, level, false);
            }
        }
    }

    private void checkOrphaned(Display.TextDisplay display, ServerLevel level) {
        level.getProfiler().push("jsst_world_containers");
        var linkedPos = ((JSSTLinkedToPos) display).getLinked();
        if (linkedPos != null) {
            var linkedBe = level.getBlockEntity(linkedPos);
            if (linkedBe != null && displayCache.containsKey(linkedBe)) {
                displayCache.put(linkedBe, display);
                return;
            }
        }
        display.remove(Entity.RemovalReason.DISCARDED);
        level.getProfiler().pop();
    }

    @Override
    public void init() {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            level.getProfiler().push("jsst_world_containers");
            for (Triple<BlockPos, ServerLevel, Boolean> triple : delayedChecks.removeAll(level.getGameTime()))
                checkBlockEntity(triple.getLeft(), triple.getMiddle(), triple.getRight());
            level.getProfiler().pop();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            delayedChecks.clear();
            displayCache.clear();
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (getConfig().enabled)
                delayedChecks.put(level.getGameTime() + 1, Triple.of(be.getBlockPos(), level, true));
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(this::removeText);

        // Clean orphaned, e.g. if the server crashes, or removal did not happen in any other case.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Display.TextDisplay display && entity.getTags().contains(JSST_TAG)) {
                if (!getConfig().enabled)
                    display.remove(Entity.RemovalReason.DISCARDED);
                else if (displayCache.containsValue(display))
                    checkOrphaned(display, level);
            }
        });
    }

    @Override
    public String id() {
        return "worldContainerNames";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().worldContainerNames;
    }

    @Override
    public void onDisabled() {
        delayedChecks.clear();
        for (Display.TextDisplay display : displayCache.values())
            display.remove(Entity.RemovalReason.DISCARDED);
        displayCache.clear();
    }

    public enum DisplayMode {
        INVENTORY_NAME,
        INVENTORY_NAME_WITH_ITEM_PARSING,
        MOST_COMMON_ITEM
    }

    public static class Config extends Feature.Config {
        public DisplayMode displayMode = DisplayMode.INVENTORY_NAME;
    }
}
