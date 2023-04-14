package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Locale;

public record Colour(Integer value) {
    public static Colour fromRgb(int r, int g, int b) {
        return new Colour(FastColor.ARGB32.color(0, r, g, b));
    }

    public static Colour fromHsv(float hue, float saturation, float value) {
        return new Colour(Mth.hsvToRgb(hue, saturation, value));
    }

    /**
     * Calculate the hue, saturation and value of this colour.
     * @return The hue, in a range between 0 and 1.
     */
    public Triple<Float, Float, Float> hsv() {
        float r = r() / 255f;
        float g = g() / 255f;
        float b = b() / 255f;
        var min = Math.min(Math.min(r, g), b);
        var max = Math.max(Math.max(r, g), b); // value
        var range = max - min;

        var hue = 0f;
        if (min != max) {
            if (max == r)
                hue = (g - b) / range;
            else if (max == g)
                hue = 2f + (b - r) / range;
            else
                hue = 4f + (r - g) / range;
            hue /= 6;
            if (hue < 0) hue += 1;
        }
        var saturation = max == 0f ? 0f : range/max;
        return Triple.of(hue, saturation, max);
    }

    public int r() {
        return value >> 16;
    }

    public int g() {
        return (value >> 8) & 0xff;
    }

    public int b() {
        return value & 0xff;
    }

    public String formatString() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    public Style style() {
        return Style.EMPTY.withColor(value);
    }

    public ItemStack label() {
        return Labels.create(EditorUtils.colourToItem(value)).withName(Component.literal(formatString()).withStyle(style())).build();
    }
}
