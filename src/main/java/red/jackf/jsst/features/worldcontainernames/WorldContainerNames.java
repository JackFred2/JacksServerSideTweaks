package red.jackf.jsst.features.worldcontainernames;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import red.jackf.jsst.features.Feature;

import java.util.HashMap;
import java.util.Map;

public class WorldContainerNames implements Feature {
    private static final String JSST_TAG = "jsst_world_container_name";

    private static final Map<Nameable, Display.TextDisplay> linked = new HashMap<>();

    public static Display.TextDisplay createText(ServerLevel level) {
        var entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        entity.addTag(JSST_TAG);
        entity.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        return entity;
    }

    public static void checkBlockEntity(BlockEntity be, ServerLevel level) {
        System.out.println(be);
        Component customName;
        if (be instanceof Nameable nameable && (customName = nameable.getCustomName()) != null) {
            createOrUpdateText(level, be, nameable, customName);
        }
    }

    @Override
    public void init() {
        // Clean orphaned
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Display.TextDisplay && !linked.containsValue(entity) && entity.getTags().contains(JSST_TAG)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        
        // Update on opened (and quick fix for ctrl-clicked blocks
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level instanceof ServerLevel serverLevel) {
                var be = level.getBlockEntity(hitResult.getBlockPos());
                checkBlockEntity(be, serverLevel);
            }
            return InteractionResult.PASS;
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(WorldContainerNames::checkBlockEntity);

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> {
            if (be instanceof Nameable nameable && linked.containsKey(nameable))
                linked.remove(nameable).remove(Entity.RemovalReason.DISCARDED);
        });
    }

    private static void createOrUpdateText(ServerLevel level, BlockEntity asBe, Nameable asNameable, Component text) {
        var textDisplay = linked.computeIfAbsent(asNameable, unused -> createText(level));
        textDisplay.setPos(asBe.getBlockPos().above().getCenter());
        textDisplay.setText(text);
        textDisplay.setViewRange(0.125f);
        level.addFreshEntity(textDisplay);
    }
}
