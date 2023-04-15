package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Simple Two-tone gradient
 */
public record Gradient(Colour start, Colour end, Mode mode) {
    public Colour evaluate(float factor) {
        factor = Mth.clamp(factor, 0f, 1f);
        if (mode == Mode.RGB) return Colour.fromRgb(Mth.lerpInt(factor, start.r(), end.r()), Mth.lerpInt(factor, start.g(), end.g()), Mth.lerpInt(factor, start.b(), end.b()));
        var startHsv = start.hsv();
        var endHsv = end.hsv();
        var hueRange = Math.abs(startHsv.getLeft() - endHsv.getLeft());
        var wraps = ((hueRange > 0.5f) && (mode == Mode.HSV_SHORT)) || ((hueRange <= 0.5f) && (mode == Mode.HSV_LONG));
        if (!wraps)
            return Colour.fromHsv(Mth.lerp(factor, startHsv.getLeft(), endHsv.getLeft()), Mth.lerp(factor, startHsv.getMiddle(), endHsv.getMiddle()), Mth.lerp(factor, startHsv.getRight(), endHsv.getRight()));
        else {
            var fakeTarget = endHsv.getLeft() + (endHsv.getLeft() > startHsv.getLeft() ? -1f : 1f);
            var hue = Mth.lerp(factor, startHsv.getLeft(), fakeTarget);
            if (hue < 0) hue += 1f;
            if (hue > 1) hue -= 1f;
            var saturation = Mth.lerp(factor, startHsv.getMiddle(), endHsv.getMiddle());
            var value = Mth.lerp(factor, startHsv.getRight(), endHsv.getRight());
            return Colour.fromHsv(hue, saturation, value);
        }
    }

    public enum Mode implements Selector.Labeled {
        HSV_SHORT("HSV Short", new ItemStack(Items.PAPER)),
        HSV_LONG("HSV Long", new ItemStack(Items.PAPER, 2)),
        RGB("RGB", new ItemStack(Items.PAPER, 3));

        private final String settingName;
        private final ItemStack label;

        Mode(String settingName, ItemStack label) {
            this.settingName = settingName;
            this.label = label;
        }

        public ItemStack label() {
            return label;
        }

        public String settingName() {
            return settingName;
        }
    }
}
