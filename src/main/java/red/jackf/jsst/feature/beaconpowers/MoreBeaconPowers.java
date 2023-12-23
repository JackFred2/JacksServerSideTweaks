package red.jackf.jsst.feature.beaconpowers;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
    public Config config() {
        return JSST.CONFIG.instance().moreBeaconPowers;
    }

    public static class Config extends ToggleFeature.Config {
        public Config() {
            // start disabled
            this.enabled = false;
        }

        @Comment("""
                This feature can optionally increase the maximum level of the beacon. This allows you to limit more powerful effects
                to higher levels.
                Options: [4, 6]
                Default: 4 (same as vanilla)""")
        public int maxBeaconLevel = 4;

        @Comment("""
                List of powers available at each tier of the beacon. Valid levels are 1 to 6, and any effects in multiple levels will
                be forced to their lowest. Primary powers are anything in levels 1 to 3, while secondary powers are 4 to 6 or a higher
                level of the primary.
                Options: A map of beacon levels to a set of status effects.
                Default: Same as vanilla's beacon.""")
        public BeaconPowers powers = BeaconPowers.getDefault();

        public void validate() {
            this.maxBeaconLevel = Mth.clamp(this.maxBeaconLevel, 4, 6);
        }
    }
}
