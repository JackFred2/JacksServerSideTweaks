package red.jackf.jsst.impl.feature.mapeditor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.RegistryUtils;

import java.util.*;

public class MapEditor {
    private static final Map<ServerPlayer, MapEditSession> SESSIONS = new HashMap<>();

    public static void setup() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer serverPlayer
                    && JSSTConfig.INSTANCE.instance().mapEditor.enabled
                    && (!JSSTConfig.INSTANCE.instance().mapEditor.requiresOp || player.hasPermissions(4))
                    && hand == InteractionHand.MAIN_HAND // using main hand
                    && hitResult != null // using the position-based callback not the positionless
                    && entity instanceof ItemFrame itemFrame
                    && itemFrame.getItem().has(DataComponents.MAP_ID) // item frame with map
                    && MapEditor.isValidTool(serverLevel.registryAccess(), player.getItemInHand(hand))) { // using feather
                MapEditor.onInteract(serverPlayer, itemFrame);

                return InteractionResult.SUCCESS;
            }

            // stop other people rotating the frame while in use
            if (level instanceof ServerLevel && entity instanceof ItemFrame frame && existsSessionUsingFrame(frame)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });


        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (Iterator<MapEditSession> iterator = SESSIONS.values().iterator(); iterator.hasNext(); ) {
                MapEditSession value = iterator.next();
                if (!value.stillValid()) {
                    iterator.remove();
                    value.end();
                }
            }
        });
    }

    public static boolean existsSessionUsingFrame(ItemFrame frame) {
        return SESSIONS.values().stream().anyMatch(session -> session.entity() == frame);
    }

    private static boolean existsSessionWithMapId(MapId id) {
        return SESSIONS.values().stream().anyMatch(session -> session.getMapId() == id);
    }

    private static void onInteract(ServerPlayer player, ItemFrame frame) {
        MapEditSession existingSession = SESSIONS.get(player);

        if (existingSession != null && existingSession.entity() != frame) return;

        if (existingSession == null) {
            if (existsSessionUsingFrame(frame) || existsSessionWithMapId(frame.getItem().get(DataComponents.MAP_ID))) {
                player.sendSystemMessage(Component.translatable("jsst.mapEditor.alreadyBeingEdited"));
                return;
            }
            startSession(player, frame, frame.getItem().get(DataComponents.MAP_ID));
        }
    }

    private static void startSession(ServerPlayer player, ItemFrame frame, MapId id) {
        MapEditSession session = new MapEditSession(player, frame, id);
        SESSIONS.put(player, session);

        session.start();
    }

    static boolean isValidTool(RegistryAccess registries, ItemStack item) {
        HolderSet<Item> valid = RegistryUtils.getValuesFromIDOrTag(registries, Registries.ITEM, JSSTConfig.INSTANCE.instance().mapEditor.tool);

        return item.is(valid);
    }
}
