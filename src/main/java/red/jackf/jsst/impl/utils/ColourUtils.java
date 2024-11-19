package red.jackf.jsst.impl.utils;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public interface ColourUtils {
    /**
     * Order of dyed items in creative menus
     */
    List<DyeColor> CANON_DYE_ORDER = List.of(
            DyeColor.WHITE,
            DyeColor.LIGHT_GRAY,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.BROWN,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK
    );

    Map<DyeColor, Item> STAINED_GLASS = Map.ofEntries(
            Map.entry(DyeColor.WHITE, Items.WHITE_STAINED_GLASS_PANE),
            Map.entry(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_STAINED_GLASS_PANE),
            Map.entry(DyeColor.GRAY, Items.GRAY_STAINED_GLASS_PANE),
            Map.entry(DyeColor.BLACK, Items.BLACK_STAINED_GLASS_PANE),
            Map.entry(DyeColor.BROWN, Items.BROWN_STAINED_GLASS_PANE),
            Map.entry(DyeColor.RED, Items.RED_STAINED_GLASS_PANE),
            Map.entry(DyeColor.ORANGE, Items.ORANGE_STAINED_GLASS_PANE),
            Map.entry(DyeColor.YELLOW, Items.YELLOW_STAINED_GLASS_PANE),
            Map.entry(DyeColor.LIME, Items.LIME_STAINED_GLASS_PANE),
            Map.entry(DyeColor.GREEN, Items.GREEN_STAINED_GLASS_PANE),
            Map.entry(DyeColor.CYAN, Items.CYAN_STAINED_GLASS_PANE),
            Map.entry(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_STAINED_GLASS_PANE),
            Map.entry(DyeColor.BLUE, Items.BLUE_STAINED_GLASS_PANE),
            Map.entry(DyeColor.PURPLE, Items.PURPLE_STAINED_GLASS_PANE),
            Map.entry(DyeColor.MAGENTA, Items.MAGENTA_STAINED_GLASS_PANE),
            Map.entry(DyeColor.PINK, Items.PINK_STAINED_GLASS_PANE)
    );
}
