package red.jackf.jsst.features.itemeditor.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;

public class EditorUtils {
    public static final Style CLEAN = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false);
    private static final ItemStack DIVIDER = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(literal(""));

    public static ItemGuiElement divider() {
        return new ItemGuiElement(DIVIDER, null);
    }

    public static ItemGuiElement reset(Runnable onClick) {
        return new ItemGuiElement(makeLabel(Items.NAUTILUS_SHELL, "Reset"), onClick);
    }

    public static ItemGuiElement clear(Runnable onClick) {
        return new ItemGuiElement(makeLabel(Items.WATER_BUCKET, "Clear"), onClick);
    }

    public static ItemGuiElement cancel(Runnable onClick) {
        return new ItemGuiElement(makeLabel(Items.BARRIER, "Cancel"), onClick);
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

    public static ItemStack withLore(ItemStack input, Component component) {
        var stack = input.copy();
        var display = stack.getOrCreateTagElement("display");
        if (!display.contains(ItemStack.TAG_LORE, Tag.TAG_LIST)) display.put(ItemStack.TAG_LORE, new ListTag());
        var list = display.getList(ItemStack.TAG_LORE, Tag.TAG_LIST);
        list.add(StringTag.valueOf(Component.Serializer.toJson(component)));
        return stack;
    }

    public static ItemStack withHint(ItemStack input, String text) {
        return withLore(input, Component.literal(text)
                .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#70FF68")).withItalic(false)));
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

    /**
     * Creates a label from an ItemStack, named `text` with no italics.
     * @param stack Stack to make a label of
     * @param text Name for the label stack
     * @param hintText Green hint text added to this label
     */
    public static ItemStack makeLabel(ItemStack stack, Component text, @Nullable String hintText) {
        var newStack = stack.copy();
        newStack.setHoverName(text);
        for (ItemStack.TooltipPart part : ItemStack.TooltipPart.values())
            newStack.hideTooltipPart(part);
        if (hintText != null)
            return withHint(newStack, hintText);
        return newStack;
    }

    public static ItemStack makeLabel(ItemLike stack, Component text, @Nullable String hintText) {
        return makeLabel(new ItemStack(stack), text, hintText);
    }

    public static ItemStack makeLabel(ItemLike stack, Component text) {
        return makeLabel(new ItemStack(stack), text, null);
    }

    public static ItemStack makeLabel(ItemStack stack, String text, @Nullable String hintText) {
        return makeLabel(stack, literal(text).withStyle(CLEAN), hintText);
    }

    public static ItemStack makeLabel(ItemLike item, String text, @Nullable String hintText) {
        return makeLabel(item, literal(text).withStyle(CLEAN), hintText);
    }

    public static ItemStack makeLabel(ItemStack stack, String text) {
        return makeLabel(stack, text, null);
    }

    public static ItemStack makeLabel(ItemLike item, String text) {
        return makeLabel(new ItemStack(item), text);
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
    public static void drawPage(Map<Integer, ItemGuiElement> elements, List<?> items, int page, int maxPage, RowFiller rowFiller, Consumer<Integer> itemRemover, @Nullable Runnable itemAdder, Consumer<Integer> pageChanger) {
        // Page Buttons
        if (page > 0)
            elements.put(51, new ItemGuiElement(EditorUtils.makeLabel(Items.RED_CONCRETE, "Previous Page"), () -> pageChanger.accept(Math.max(0, page - 1))));
        if (maxPage != 0)
            elements.put(52, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, "Page %s/%s".formatted(page + 1, maxPage + 1)), null));
        if (page < maxPage)
            elements.put(53, new ItemGuiElement(EditorUtils.makeLabel(Items.LIME_CONCRETE, "Next Page"), () -> pageChanger.accept(Math.min(maxPage, page + 1))));

        var itemsToDraw = items.subList(page * 5, Math.min(page * 5 + 5, items.size()));
        int row;
        for (row = 0; row < itemsToDraw.size(); row++) {
            var startPos = 4 + (row * 9);
            var itemIndex = (page * 5) + row;
            rowFiller.fill(startPos, itemIndex);
            elements.put(startPos + 4, new ItemGuiElement(makeLabel(Items.BARRIER, "Delete"), () -> itemRemover.accept(itemIndex)));
        }

        if (page == maxPage && itemAdder != null) {
            elements.put(row * 9 + 4, new ItemGuiElement(EditorUtils.makeLabel(Items.NETHER_STAR, "Add"), itemAdder));
        }
    }

    public interface RowFiller {
        void fill(Integer slotStartPos, Integer itemIndex);
    }
}
