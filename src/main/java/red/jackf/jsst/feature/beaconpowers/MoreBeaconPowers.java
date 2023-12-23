package red.jackf.jsst.feature.beaconpowers;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

public class MoreBeaconPowers extends ToggleFeature<MoreBeaconPowers.Config> {
    public static final MoreBeaconPowers INSTANCE = new MoreBeaconPowers();
    private MoreBeaconPowers() {}

    @Override
    public void setup() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            BlockEntity be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof BeaconBlockEntity bbe && config().enabled) {
                new ExpandedBeaconScreen(serverPlayer, bbe).open();
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        });
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().moreBeaconPowers;
    }

    public static class Config extends ToggleFeature.Config {
        public Config() {
            // start disabled
            this.enabled = false;
        }
    }
}
