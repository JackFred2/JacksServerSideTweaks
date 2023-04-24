package red.jackf.jsst.features.worldcontainernames;

import blue.endless.jankson.Comment;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Feature;

public class WorldContainerNames extends Feature<WorldContainerNames.Config> {
    private static final String JSST_TAG = "jsst_world_container_name";
    private static final Float BASE_VIEW_RANGE = 0.2f;
    private static final Float MAX_MULTIPLIER = 4f;
    private static final Float MIN_MULTIPLIER = 0.25f;

    private final BiMap<BlockEntity, Display> displayCache = HashBiMap.create();
    private long nextUpdateSchedulerTick = -1;

    // TODO: migrate to DelayedRunnables
    private final Multimap<Long, Triple<BlockPos, ServerLevel, Boolean>> delayedChecks = HashMultimap.create();

    // Creates or updates a text entity
    private void createOrUpdateDisplay(BlockEntity be, ServerLevel level) {
        var display = displayCache.get(be);
        var result = DisplayParser.parse(be);
        if (result != null) {
            if (!result.matches(display)) {
                if (result.isText()) {
                    display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
                    display.setTransformation(Transformation.identity());
                } else {
                    display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
                }
                display.addTag(JSST_TAG);
                display.setViewRange(BASE_VIEW_RANGE * getConfig().labelRangeMultiplier);
                display.setBillboardConstraints(getConfig().facingMode.constraint);
                displayCache.put(be, display);
                ((JSSTLinkedToPos) display).jsst_setLinked(be.getBlockPos());
                level.addFreshEntity(display);
            }
            if (result.isText()) {
                ((Display.TextDisplay) display).setText(result.text());
            } else {
                @SuppressWarnings("DataFlowIssue")
                var scale = result.stack().getItem() instanceof BlockItem ? 0.4f : 0.6f;
                display.setTransformation(new Transformation(null, null, new Vector3f(scale), null));
                ((Display.ItemDisplay) display).setItemStack(result.stack());
            }
            display.setPos(result.pos());
        } else if (display != null) {
            displayCache.remove(be);
            display.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    // Remove a display, updating linked
    private void removeDisplay(BlockEntity be, ServerLevel level) {
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
            createOrUpdateDisplay(be, level);
            if (propagate) {
                for (BlockPos otherPos : toUpdate)
                    checkBlockEntity(otherPos, level, false);
            }
        }
    }

    private void checkOrphaned(Display display, ServerLevel level) {
        level.getProfiler().push("jsst_world_container_names");
        var linkedPos = ((JSSTLinkedToPos) display).jsst_getLinked();
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
            level.getProfiler().push("jsst_world_container_names");
            for (Triple<BlockPos, ServerLevel, Boolean> triple : delayedChecks.removeAll(level.getGameTime()))
                checkBlockEntity(triple.getLeft(), triple.getMiddle(), triple.getRight());

            if (nextUpdateSchedulerTick < level.getGameTime()) {
                var offset = 1L;
                for (BlockEntity be : displayCache.keySet()) {
                    delayedChecks.put(level.getGameTime() + offset++, Triple.of(be.getBlockPos(), level, true));
                }

                nextUpdateSchedulerTick = level.getGameTime() + Math.max(offset, 40L);
            }
            level.getProfiler().pop();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            delayedChecks.clear();
            displayCache.clear();
            nextUpdateSchedulerTick = -1;
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (getConfig().enabled)
                delayedChecks.put(level.getGameTime() + 1, Triple.of(be.getBlockPos(), level, true));
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level instanceof ServerLevel serverLevel) {
                var be = serverLevel.getBlockEntity(hitResult.getBlockPos());
                if (be != null)
                    delayedChecks.put(serverLevel.getGameTime() + 1, Triple.of(hitResult.getBlockPos(), serverLevel, true));
            }
            return InteractionResult.PASS;
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(this::removeDisplay);

        // Clean orphaned, e.g. if the server crashes, or removal did not happen in any other case.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Display display && entity.getTags().contains(JSST_TAG)) {
                if (!getConfig().enabled)
                    display.remove(Entity.RemovalReason.DISCARDED);
                else if (displayCache.containsValue(display))
                    checkOrphaned(display, level);
            }
        });

        getConfig().labelRangeMultiplier = Mth.clamp(getConfig().labelRangeMultiplier, MIN_MULTIPLIER, MAX_MULTIPLIER);
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
        for (Display display : displayCache.values())
            display.remove(Entity.RemovalReason.DISCARDED);
        displayCache.clear();
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        node.then(OptionBuilders.withFloatRange("labelRangeMultiplier", MIN_MULTIPLIER, MAX_MULTIPLIER, () -> getConfig().labelRangeMultiplier, range -> {
            getConfig().labelRangeMultiplier = range;
            for (Display display : displayCache.values())
                display.setViewRange(BASE_VIEW_RANGE * getConfig().labelRangeMultiplier);
        }));

        node.then(OptionBuilders.withEnum("facingMode", FacingMode.class, () -> getConfig().facingMode,  mode -> {
            getConfig().facingMode = mode;
            for (Display display : displayCache.values())
                display.setBillboardConstraints(mode.constraint);
        }));
    }

    public enum FacingMode implements StringRepresentable {
        CENTER(Display.BillboardConstraints.CENTER),
        VERTICAL(Display.BillboardConstraints.VERTICAL);

        private final Display.BillboardConstraints constraint;

        FacingMode(Display.BillboardConstraints constraint) {
            this.constraint = constraint;
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return constraint.getSerializedName();
        }
    }

    public static class Config extends Feature.Config {
        @Comment("How labels should face the player. (Default: CENTER, Options: CENTER, VERTICAL)")
        public FacingMode facingMode = FacingMode.CENTER;
        @Comment("Multiplier for the distance labels are shown. (Default: 1, Range: [0.25, 4])")
        public Float labelRangeMultiplier = 1.0f;
    }
}
