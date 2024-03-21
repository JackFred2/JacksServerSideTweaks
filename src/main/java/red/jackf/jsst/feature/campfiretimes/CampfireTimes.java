package red.jackf.jsst.feature.campfiretimes;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

import java.util.HashMap;
import java.util.Map;

public class CampfireTimes extends ToggleFeature<CampfireTimes.Config> {
    public static final CampfireTimes INSTANCE = new CampfireTimes();
    private CampfireTimes() {}

    private static final float TEXT_SCALE = 0.6f;
    private static final float TEXT_X_OFFSET = 0.3f;
    private static final float TEXT_Y_OFFSET = 0.2f;
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
                        .build(false), Lists.newArrayList(null, null, null, null));
                LIES.put(cbe, lie);
            }

            EntityLie<Display.TextDisplay> entityLie;
            if (lie.lies().get(slot) == null) {
                int dir = (slot + cbe.getBlockState().getValue(CampfireBlock.FACING).get2DDataValue()) % 4;
                Vec3 offset = new Vec3(
                        dir == 1 || dir == 2 ? TEXT_X_OFFSET : -TEXT_X_OFFSET,
                        TEXT_Y_OFFSET,
                        dir == 2 || dir == 3 ? TEXT_X_OFFSET : -TEXT_X_OFFSET
                );
                
                entityLie = EntityLie.builder(EntityBuilders.textDisplay(serverLevel)
                        .billboard(Display.BillboardConstraints.CENTER)
                        .scale(new Vector3f(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE))
                        .position(cbe.getBlockPos().getCenter().add(offset))
                        .viewRangeModifier(1f / 16)
                        .seeThrough(true)
                        .build()).createAndShow();
                lie.add(entityLie, slot);
            } else {
                entityLie = lie.lies().get(slot);
            }

            EntityUtils.setDisplayText(entityLie.entity(), Component.literal(Mth.positiveCeilDiv(progress * 100, totalTime) + "%"));
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
