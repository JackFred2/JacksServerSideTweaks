package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.itemeditor.editors.LoreEditor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

    public static MenuProvider make9x1(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::oneRow, title, elements);
    }

    public static MenuProvider make9x3(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::threeRows, title, elements);
    }

    public static MenuProvider make9x4(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::fourRows, title, elements);
    }

    public static MenuProvider make9x5(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::fiveRows, title, elements);
    }

    public static MenuProvider make9x6(Component title, Map<Integer, ItemGuiElement> elements) {
        return make(ChestMenu::sixRows, title, elements);
    }

    private static MenuProvider make(BiFunction<Integer, Inventory, ChestMenu> menuFunc, Component title, Map<Integer, ItemGuiElement> elements) {
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
                    if (slot.container != menu.getContainer()) continue;
                    if (elements.containsKey(slot.index)) {
                        slot.set(elements.get(slot.index).label());
                    } /*else {
                        slot.set(BLANK.copy());
                    }*/
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
        var r = (colour.getValue() >> 16 & 255);
        var g = (colour.getValue() >> 8 & 255);
        var b = colour.getValue() & 255;
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
    public static void drawPage(Map<Integer, ItemGuiElement> elements, List<?> items, int page, int maxPage, Consumer<Integer> pageChanger, RowFiller rowFiller, Consumer<Integer> itemRemover, Runnable itemAdder) {
        // Page Buttons
        if (page > 0)
            elements.put(51, new ItemGuiElement(Labels.create(Items.RED_CONCRETE).withName("Previous Page").build(), () -> pageChanger.accept(Math.max(0, page - 1))));
        if (maxPage != 0)
            elements.put(52, new ItemGuiElement(Labels.create(Items.PAPER).withName("Page %s/%s".formatted(page + 1, maxPage + 1)).build(), null));
        if (page < maxPage)
            elements.put(53, new ItemGuiElement(Labels.create(Items.LIME_CONCRETE).withName("Next Page").build(), () -> pageChanger.accept(Math.min(maxPage, page + 1))));

        var itemsToDraw = items.subList(page * 5, Math.min(page * 5 + 5, items.size()));
        int row;
        for (row = 0; row < itemsToDraw.size(); row++) {
            var startPos = 4 + (row * 9);
            var itemIndex = (page * 5) + row;
            rowFiller.fill(startPos, itemIndex);
            elements.put(startPos + 4, new ItemGuiElement(Labels.create(Items.BARRIER).withName("Delete").build(), () -> itemRemover.accept(itemIndex)));
        }

        if (itemsToDraw.size() != 5) {
            elements.put(row * 9 + 4, new ItemGuiElement(Labels.create(Items.NETHER_STAR).withName("Add").build(), itemAdder));
        }
    }

    public interface RowFiller {
        void fill(Integer slotStartPos, Integer itemIndex);
    }
}
