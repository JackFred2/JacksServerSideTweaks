package red.jackf.jsst.impl.mixinutils;

import net.minecraft.world.entity.Display;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

import java.util.List;

public interface JSSTCampfireExt {
    List<EntityLie<Display.TextDisplay>> jsst$getLies();

    int jsst$getCookingProgress(int slot);

    int jsst$getCookingTime(int slot);
}
