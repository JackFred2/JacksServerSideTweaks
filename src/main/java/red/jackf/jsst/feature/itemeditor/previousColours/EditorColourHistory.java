package red.jackf.jsst.feature.itemeditor.previousColours;

import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;

import java.util.List;

public interface EditorColourHistory {

    List<Colour> jsst$itemEditor$getPreviousColours();

    List<Gradient> jsst$itemEditor$getPreviousGradients();

    void jsst$itemEditor$push(Gradient gradient);
}
