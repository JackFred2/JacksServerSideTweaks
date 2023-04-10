package red.jackf.jsst.features.itemeditor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import java.util.Map;
import java.util.function.BiFunction;

import static net.minecraft.network.chat.Component.literal;

public class EditorUtils {
    public static final Style CLEAN = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false);
    public static final ItemStack DIVIDER = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(literal(""));

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
    static ItemStack makeLabel(ItemStack stack, Component text, @Nullable String hintText) {
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

}
