package red.jackf.jsst.feature.itemeditor.previousColours;

import net.minecraft.server.level.ServerPlayer;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;

import java.util.List;

public interface EditorColourHistory {

    List<Colour> jsst$itemEditor$getPreviousColours();

    List<Gradient> jsst$itemEditor$getPreviousGradients();

    void jsst$itemEditor$push(Gradient gradient);

    void jsst$itemEditor$copyFrom(ServerPlayer oldPlayer);
}
