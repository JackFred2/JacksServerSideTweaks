package red.jackf.jsst.impl.feature.mapeditor;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.RegistryUtils;

public class MapEditor {
    public static void setup() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer serverPlayer
                    && entity instanceof ItemFrame itemFrame
                    && hand == InteractionHand.MAIN_HAND
                    && hitResult != null
                    && MapEditor.isValidTool(serverLevel.registryAccess(), player.getItemInHand(hand))) {
                MapEditor.onInteract(serverLevel, serverPlayer, itemFrame, hitResult);
            }

            return InteractionResult.PASS;
        });
    }

    private static void onInteract(ServerLevel level, ServerPlayer player, ItemFrame frame, EntityHitResult hit) {

    }

    private static boolean isValidTool(RegistryAccess registries, ItemStack item) {
        HolderSet<Item> valid = RegistryUtils.getValuesFromIDOrTag(registries, Registries.ITEM, JSSTConfig.INSTANCE.instance().mapEditor.tool);

        return item.is(valid);
    }
}
