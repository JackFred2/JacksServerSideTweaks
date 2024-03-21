package red.jackf.jsst.feature.campfiretimes;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

import java.util.List;

public final class CampfireTimeLie {
    private static final float TEXT_SCALE = 0.6f;
    private static final float TEXT_X_OFFSET = 0.3f;
    private static final float TEXT_Y_OFFSET = 0.2f;
    private final Tracker<EntityLie<? extends Display>> tracker;
    private final List<EntityLie<Display.TextDisplay>> lies;

    public CampfireTimeLie(Tracker<EntityLie<? extends Display>> tracker) {
        this.tracker = tracker;
        this.lies = Lists.newArrayList(null, null, null, null);
    }

    public void fade() {
        for (EntityLie<Display.TextDisplay> lie : lies) {
            if (lie != null) lie.fade();
        }
        tracker.setRunning(false);
    }

    public boolean isEmpty() {
        for (EntityLie<Display.TextDisplay> lie : this.lies) {
            if (lie != null) return false;
        }
        return true;
    }

    private void add(EntityLie<Display.TextDisplay> display, int slot) {
        this.lies.set(slot, display);
        this.tracker.addLie(display);
        this.tracker.setRunning(false);
        this.tracker.setRunning(true);
    }

    public void remove(int slot) {
        if (this.lies.get(slot) != null) {
            this.lies.get(slot).fade();
            this.lies.set(slot, null);
        }
    }

    public void setText(CampfireBlockEntity cbe, int slot, Component text) {
        if (this.lies.get(slot) == null) {
            int dir = (slot + cbe.getBlockState().getValue(CampfireBlock.FACING).get2DDataValue()) % 4;
            Vec3 offset = new Vec3(
                    dir == 1 || dir == 2 ? TEXT_X_OFFSET : -TEXT_X_OFFSET,
                    TEXT_Y_OFFSET,
                    dir == 2 || dir == 3 ? TEXT_X_OFFSET : -TEXT_X_OFFSET
            );

            this.add(EntityLie.builder(EntityBuilders.textDisplay((ServerLevel) cbe.getLevel())
                    .billboard(Display.BillboardConstraints.CENTER)
                    .scale(new Vector3f(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE))
                    .position(cbe.getBlockPos().getCenter().add(offset))
                    .viewRangeModifier(1f / 16)
                    .seeThrough(true)
                    .build()).createAndShow(), slot);
        }

        EntityUtils.setDisplayText(this.lies.get(slot).entity(), text);
    }
}
