package red.jackf.jsst.feature.campfiretimes;

import net.minecraft.world.entity.Display;
import red.jackf.jackfredlib.api.lying.Tracker;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

import java.util.List;

public record CampfireTimeLie(Tracker<EntityLie<? extends Display>> tracker,
                              List<EntityLie<Display.TextDisplay>> lies) {

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

    public void add(EntityLie<Display.TextDisplay> display, int slot) {
        if (this.lies.get(slot) != null) {
            this.lies.get(slot).fade();
        }

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
}
