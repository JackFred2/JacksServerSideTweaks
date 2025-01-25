package red.jackf.jsst.impl.feature.extrahighlights;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;
import red.jackf.jsst.impl.config.JSSTConfig;

import java.util.HashSet;
import java.util.Set;

public class SugarcaneHighlights {
    private static final Set<ItemEntity> hasHighlight = new HashSet<>();

    public static void setup() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> hasHighlight.clear());

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ItemEntity && world instanceof ServerLevel)
                hasHighlight.remove(entity);
        });
    }

    public static void onSugarcaneItemTick(ServerLevel serverLevel, ItemEntity itemEntity) {
        var config = JSSTConfig.INSTANCE.instance().extraHighlights;

        if (!config.sugarcaneEnabled || hasHighlight.contains(itemEntity)) return;

        var lie = EntityGlowLie.builder(itemEntity)
                .colour(ChatFormatting.GOLD)
                .createAndShow();

        hasHighlight.add(itemEntity);

        Tracker.builder(serverLevel)
                .setAround(itemEntity, 16d)
                .addPredicate(SugarcaneHighlights::holdingSugarcane)
                .setUpdateInterval(10L)
                .addLie(lie)
                .build(true);
    }

    private static boolean holdingSugarcane(ServerPlayer player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.SUGAR_CANE) ||
               player.getItemInHand(InteractionHand.OFF_HAND).is(Items.SUGAR_CANE);
    }
}
