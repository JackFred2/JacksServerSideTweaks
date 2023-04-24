package red.jackf.jsst.features.itemeditor.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.itemeditor.editors.LoreEditor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static net.minecraft.network.chat.Component.literal;

public class EditorUtils {
    private static final ItemStack DIVIDER = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(literal(""));

    public static ItemGuiElement divider() {
        return new ItemGuiElement(DIVIDER, null);
    }

    public static ItemGuiElement reset(Runnable onClick) {
        return new ItemGuiElement(Labels.create(Items.NAUTILUS_SHELL).withName("Reset").build(), onClick);
    }

    public static ItemGuiElement clear(Runnable onClick) {
        return new ItemGuiElement(Labels.create(Items.WATER_BUCKET).withName("Clear").build(), onClick);
    }

    public static ItemGuiElement cancel(Runnable onClick) {
        return new ItemGuiElement(Labels.create(Items.BARRIER).withName("Cancel").build(), onClick);
    }

    public static MenuProvider make5x1(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(HopperMenu::new, title, elements);
    }

    public static MenuProvider make9x1(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::oneRow, title, elements);
    }

    public static MenuProvider make9x2(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::twoRows, title, elements);
    }

    public static MenuProvider make9x3(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::threeRows, title, elements);
    }

    @SuppressWarnings("unused")
    public static MenuProvider make9x4(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::fourRows, title, elements);
    }

    public static MenuProvider make9x5(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::fiveRows, title, elements);
    }

    public static MenuProvider make9x6(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::sixRows, title, elements);
    }

    private static MenuProvider make(BiFunction<Integer, Inventory, AbstractContainerMenu> menuFunc, Component title, Map<Integer, ItemGuiElement> elements) {
        return new MenuProvider() {
            @NotNull
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                var menu = menuFunc.apply(i, inventory);
                for (var slot : menu.slots) {
                    if (slot.container == inventory) continue;
                    if (elements.containsKey(slot.index)) {
                        slot.set(elements.get(slot.index).label());
                    }
                }
                ((JSSTSealableMenuWithButtons) menu).jsst_sealWithButtons(elements);
                return menu;
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return false;
            }
        };
    }

    public static ItemStack withHint(ItemStack input, String text) {
        return LoreEditor.mergeLore(input, List.of(Component.literal(text).withStyle(Labels.HINT)));
    }

    /**
     * Takes a TextColor and returns a dye item representing the closest value.
     */
    public static Item colourToItem(@Nullable TextColor colour) {
        if (colour == null) return Items.GUNPOWDER;
        return colourToItem(colour.getValue());
    }

    public static Item colourToItem(Integer colour) {
        var r = (colour >> 16 & 255);
        var g = (colour >> 8 & 255);
        var b = colour & 255;
        var closest = Arrays.stream(DyeColor.values()).min(Comparator.comparingInt(dye -> {
            var rDiff = (dye.getTextColor() >> 16 & 255) - r;
            var gDiff = (dye.getTextColor() >> 8 & 255) - g;
            var bDiff = dye.getTextColor() & 255 - b;
            return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
        }));
        return DyeItem.byColor(closest.orElse(DyeColor.WHITE));
    }

    public static MutableComponent mergeComponents(List<Component> components) {
        var start = literal("");
        components.forEach(c -> start.append(c.copy()));
        return start;
    }

    /**
     * Creates a page system in the right 5 columns of a 9x6 container. Creates a row of 5 with the bottom row used for
     * the page buttons and new button.
     */
    public static void drawPage(Map<Integer, ItemGuiElement> elements, List<?> items, int page, int maxPage, Consumer<Integer> pageChanger, int rowsFromBottom, RowFiller rowFiller, Consumer<Integer> itemRemover, Runnable itemAdder) {
        // Page Buttons
        if (page > 0)
            elements.put(51, new ItemGuiElement(Labels.create(Items.RED_CONCRETE).withName("Previous Page").build(), () -> pageChanger.accept(Math.max(0, page - 1))));
        if (maxPage != 0)
            elements.put(52, new ItemGuiElement(Labels.create(Items.PAPER).withName("Page %s/%s".formatted(page + 1, maxPage + 1)).build(), null));
        if (page < maxPage)
            elements.put(53, new ItemGuiElement(Labels.create(Items.LIME_CONCRETE).withName("Next Page").build(), () -> pageChanger.accept(Math.min(maxPage, page + 1))));

        var itemsPerPage = rowsFromBottom - 1;
        var startRow = 6 - rowsFromBottom;

        var itemsToDraw = items.subList(page * itemsPerPage, Math.min(page * itemsPerPage + itemsPerPage, items.size()));
        int row;
        for (row = startRow; row < itemsToDraw.size() + startRow; row++) {
            var startPos = 4 + (row * 9);
            var itemIndex = (page * itemsPerPage) + (row - startRow);
            rowFiller.fill(startPos, itemIndex);
            elements.put(startPos + 4, new ItemGuiElement(Labels.create(Items.BARRIER).withName("Delete").build(), () -> itemRemover.accept(itemIndex)));
        }

        if (itemsToDraw.size() != itemsPerPage) {
            elements.put(row * 9 + 4, new ItemGuiElement(Labels.create(Items.NETHER_STAR).withName("Add").build(), itemAdder));
        }
    }

    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> linkedMapCollector(Function<? super T, ? extends K> keyFunc, Function<? super T, ? extends V> valueFunc) {
        return Collectors.toMap(keyFunc, valueFunc, (p1, p2) -> p2, LinkedHashMap::new);
    }

    public static <K, V> Collector<Pair<K, V>, ?, LinkedHashMap<K, V>> pairLinkedMapCollector() {
        return Collectors.toMap(Pair::getFirst, Pair::getSecond, (p1, p2) -> p2, LinkedHashMap::new);
    }

    public static String formatDuration(int ticks) {
        if (ticks == -1) return "infinite";
        var formatStr = ticks == 1 ? "%s (%d tick)" : "%s (%d ticks)";
        return formatStr.formatted(StringUtil.formatTickDuration(ticks), ticks);
    }

    public interface RowFiller {
        void fill(Integer slotStartPos, Integer itemIndex);
    }
}
