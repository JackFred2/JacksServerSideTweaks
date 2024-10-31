package red.jackf.jsst.impl.feature.mapeditor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.RegistryUtils;

import java.util.*;

public class MapEditor {
    private static final Map<ServerPlayer, MapEditSession> SESSIONS = new HashMap<>();

    public static void setup() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer serverPlayer
                    && hand == InteractionHand.MAIN_HAND
                    && hitResult != null
                    && entity instanceof ItemFrame itemFrame
                    && itemFrame.getItem().is(Items.FILLED_MAP)
                    && MapEditor.isValidTool(serverLevel.registryAccess(), player.getItemInHand(hand))
                    && !SESSIONS.containsKey(serverPlayer)) {
                MapEditor.onInteract(serverPlayer, itemFrame, hitResult);

                return InteractionResult.SUCCESS;
            }

            if (level instanceof ServerLevel && entity instanceof ItemFrame frame && getSessionWith(frame).isPresent()) {
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
                } else {
                    value.tick();
                }
            }
        });
    }

    private static Optional<MapEditSession> getSessionWith(ItemFrame frame) {
        return SESSIONS.values().stream()
                .filter(session -> session.entity() == frame)
                .findFirst();
    }

    private static void onInteract(ServerPlayer player, ItemFrame frame, EntityHitResult hit) {
        Optional<MapEditSession> existingForFrame = getSessionWith(frame);

        if (existingForFrame.isPresent()) {
            player.sendSystemMessage(Component.translatable("jsst.mapEditor.frameAlreadyBeingEdited"));
            return;
        }

        startSession(player, frame);
    }

    private static void startSession(ServerPlayer player, ItemFrame frame) {
        MapEditSession session = new MapEditSession(player, frame);
        SESSIONS.put(player, session);

        session.start();
    }

    static boolean isValidTool(RegistryAccess registries, ItemStack item) {
        HolderSet<Item> valid = RegistryUtils.getValuesFromIDOrTag(registries, Registries.ITEM, JSSTConfig.INSTANCE.instance().mapEditor.tool);

        return item.is(valid);
    }
}
