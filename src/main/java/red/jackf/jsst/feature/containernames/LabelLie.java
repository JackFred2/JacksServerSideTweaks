package red.jackf.jsst.feature.containernames;

import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import red.jackf.jackfredlib.api.lying.Lie;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

public record LabelLie(Tracker<EntityLie<? extends Display>> tracker, Vec3 position) {
    public void fade() {
        tracker.getManagedLies().forEach(Lie::fade);
        tracker.setRunning(false);
    }
}
