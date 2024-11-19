package red.jackf.jsst.impl.feature.beaconenhancement;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.mixins.beaconenhancement.BeaconBlockEntityAccessor;

public class BeaconEnhancement {
    public static void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();

            if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof BeaconBlockEntity bbe && JSSTConfig.INSTANCE.instance().beaconEnhancement.enabled) {
                if (BaseContainerBlockEntity.canUnlock(player, ((BeaconBlockEntityAccessor) bbe).getLockKey(), bbe.getDisplayName())) {
                    new AltBeaconMenu(serverPlayer, bbe, ContainerLevelAccess.create(level, pos)).open();
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.PASS;
        });
    }
}
