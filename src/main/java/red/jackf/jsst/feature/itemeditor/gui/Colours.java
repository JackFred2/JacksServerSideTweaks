package red.jackf.jsst.feature.itemeditor.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DataFlowIssue")
public class Colours {
    public static final List<DyeColor> CANON_DYE_ORDER = List.of(
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

    public static final Map<ItemStack, Integer> DYES = new LinkedHashMap<>();
    public static final Map<ItemStack, Integer> CHAT_FORMATS = new LinkedHashMap<>();
    public static final Map<ItemStack, Integer> EXTRA = new LinkedHashMap<>();

    private static void put(Map<ItemStack, Integer> map, ItemStack stack, int colour) {
        map.put(stack.setHoverName(stack.getHoverName().copy().withColor(colour)), colour);
    }

    private static ItemStack potion(Item item, Potion potion) {
        return GuiElementBuilder.from(PotionUtils.setPotion(new ItemStack(item), potion)).hideFlags().asStack();
    }

    static {

        for (DyeColor colour : CANON_DYE_ORDER) {
            put(DYES, CommonLabels.simple(DyeItem.byColor(colour), Translations.dye(colour)), colour.getTextColor());
        }

        for (ChatFormatting format : ChatFormatting.values()) {
            if (!format.isColor()) continue;
            ItemStack stack = GuiElementBuilder.from(new ItemStack(Items.LEATHER_CHESTPLATE))
                    .setName(Component.literal("&" + format.getChar()))
                    .addLoreLine(Component.literal(format.getName()).withStyle(Styles.MINOR_LABEL))
                    .hideFlags().asStack();
            ((DyeableLeatherItem) Items.LEATHER_CHESTPLATE).setColor(stack, format.getColor());
            put(CHAT_FORMATS, stack, format.getColor());
        }

        put(EXTRA, CommonLabels.simple(Items.APPLE, Component.translatable("jsst.itemEditor.colour.common")), ChatFormatting.WHITE.getColor());
        put(EXTRA, CommonLabels.simple(Items.EXPERIENCE_BOTTLE, Component.translatable("jsst.itemEditor.colour.uncommon")), ChatFormatting.YELLOW.getColor());
        put(EXTRA, CommonLabels.simple(Items.GOLDEN_APPLE, Component.translatable("jsst.itemEditor.colour.rare")), ChatFormatting.AQUA.getColor());
        put(EXTRA, CommonLabels.simple(Items.ENCHANTED_GOLDEN_APPLE, Component.translatable("jsst.itemEditor.colour.epic")), ChatFormatting.LIGHT_PURPLE.getColor());
        put(EXTRA, CommonLabels.simple(Items.NAME_TAG, Component.translatable("jsst.itemEditor.colour.tooltips")), ChatFormatting.GRAY.getColor());
        put(EXTRA, CommonLabels.simple(potion(Items.POTION, Potions.HEALING), Component.translatable("jsst.itemEditor.colour.positiveTooltips")), ChatFormatting.BLUE.getColor());
        put(EXTRA, CommonLabels.simple(potion(Items.SPLASH_POTION, Potions.POISON), Component.translatable("jsst.itemEditor.colour.negativeTooltips")), ChatFormatting.RED.getColor());
        put(EXTRA, CommonLabels.simple(potion(Items.TIPPED_ARROW, Potions.REGENERATION), Component.translatable("jsst.itemEditor.colour.potionAppliedTooltips")), ChatFormatting.DARK_PURPLE.getColor());
        put(EXTRA, CommonLabels.simple(Items.DIAMOND_SWORD, Component.translatable("jsst.itemEditor.colour.weaponStats")), ChatFormatting.DARK_GREEN.getColor());
    }
}
