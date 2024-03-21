package red.jackf.jsst.feature.campfiretimes;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

import java.util.HashMap;
import java.util.Map;

public class CampfireTimes extends ToggleFeature<CampfireTimes.Config> {
    public static final CampfireTimes INSTANCE = new CampfireTimes();
    private CampfireTimes() {}

    private final Map<CampfireBlockEntity, CampfireTimeLie> LIES = new HashMap<>();

    @Override
    public void setup() {
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, level) -> {
            if (be instanceof CampfireBlockEntity cbe) {
                CampfireTimeLie removed = LIES.remove(cbe);
                if (removed != null) {
                    removed.fade();
                }
            }
        });

        // should do nothing but safe than sorry
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> LIES.clear());
    }

    public void tickRecipe(ServerLevel serverLevel,
                           CampfireBlockEntity cbe,
                           int slot,
                           int progress,
                           int totalTime) {
        boolean complete = progress >= totalTime;

        CampfireTimeLie lie = LIES.get(cbe);
        if (complete) {
            if (lie != null) {
                lie.remove(slot);
                if (lie.isEmpty()) {
                    LIES.remove(cbe, lie);
                }
            }
        } else {
            if (lie == null) {
                lie = new CampfireTimeLie(Tracker.<EntityLie<? extends Display>>builder(serverLevel)
                        .setUpdateInterval(80L)
                        .setFocus(cbe.getBlockPos().getCenter(), 32.0)
                        .build(false));
                LIES.put(cbe, lie);
            }

            lie.setText(cbe, slot, Component.literal(Mth.positiveCeilDiv(progress * 100, totalTime) + "%"));
        }
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().campfireTimes;
    }

    public void dowse(CampfireBlockEntity cbe) {
        var removed = LIES.remove(cbe);
        if (removed != null) {
            removed.fade();
        }
    }

    public static class Config extends ToggleFeature.Config {

    }
}
