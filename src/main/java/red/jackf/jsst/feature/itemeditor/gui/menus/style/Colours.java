package red.jackf.jsst.feature.itemeditor.gui.menus.style;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.Gradients;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;

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

    public static final Map<ItemStack, Colour> DYES = new LinkedHashMap<>();
    public static final Map<ItemStack, Colour> CHAT_FORMATS = new LinkedHashMap<>();
    public static final Map<ItemStack, Colour> EXTRA = new LinkedHashMap<>();
    public static final Map<ItemStack, Gradient> GRADIENTS = new LinkedHashMap<>();

    private static void put(Map<ItemStack, Colour> map, ItemStack stack, int colour) {
        map.put(stack.setHoverName(stack.getHoverName().copy().withColor(colour)), Colour.fromInt(colour));
    }

    public static void gradient(ItemStack base, Gradient gradient) {
        GRADIENTS.put(JSSTElementBuilder.from(base)
                              .addLoreLine(Util.colourise(Component.literal("|".repeat(40)), Component.empty(), gradient))
                              .asStack(), gradient);
    }

    private static ItemStack potion(Item item, Potion potion) {
        return JSSTElementBuilder.from(PotionUtils.setPotion(new ItemStack(item), potion)).hideFlags().asStack();
    }

    static {

        for (DyeColor colour : CANON_DYE_ORDER) {
            put(DYES, JSSTElementBuilder.ui(DyeItem.byColor(colour)).setName(Translations.dye(colour)).asStack(), colour.getTextColor());
        }

        for (ChatFormatting format : ChatFormatting.values()) {
            if (!format.isColor()) continue;
            ItemStack stack = JSSTElementBuilder.from(new ItemStack(Items.LEATHER_CHESTPLATE))
                    .setName(Component.literal("&" + format.getChar()))
                    .addLoreLine(Component.literal(format.getName()).withStyle(Styles.MINOR_LABEL))
                    .hideFlags().asStack();
            ((DyeableLeatherItem) Items.LEATHER_CHESTPLATE).setColor(stack, format.getColor());
            put(CHAT_FORMATS, stack, format.getColor());
        }

        put(EXTRA, JSSTElementBuilder.ui(Items.APPLE).setName(Component.translatable("jsst.itemEditor.colour.common")).asStack(), ChatFormatting.WHITE.getColor());
        put(EXTRA, JSSTElementBuilder.ui(Items.EXPERIENCE_BOTTLE).setName(Component.translatable("jsst.itemEditor.colour.uncommon")).asStack(), ChatFormatting.YELLOW.getColor());
        put(EXTRA, JSSTElementBuilder.ui(Items.GOLDEN_APPLE).setName(Component.translatable("jsst.itemEditor.colour.rare")).asStack(), ChatFormatting.AQUA.getColor());
        put(EXTRA, JSSTElementBuilder.ui(Items.ENCHANTED_GOLDEN_APPLE).setName(Component.translatable("jsst.itemEditor.colour.epic")).asStack(), ChatFormatting.LIGHT_PURPLE.getColor());
        put(EXTRA, JSSTElementBuilder.ui(Items.NAME_TAG).setName(Component.translatable("jsst.itemEditor.colour.tooltips")).asStack(), ChatFormatting.GRAY.getColor());
        put(EXTRA, JSSTElementBuilder.ui(potion(Items.POTION, Potions.HEALING)).setName(Component.translatable("jsst.itemEditor.colour.positiveTooltips")).asStack(), ChatFormatting.BLUE.getColor());
        put(EXTRA, JSSTElementBuilder.ui(potion(Items.SPLASH_POTION, Potions.POISON)).setName(Component.translatable("jsst.itemEditor.colour.negativeTooltips")).asStack(), ChatFormatting.RED.getColor());
        put(EXTRA, JSSTElementBuilder.ui(potion(Items.TIPPED_ARROW, Potions.REGENERATION)).setName(Component.translatable("jsst.itemEditor.colour.potionAppliedTooltips")).asStack(), ChatFormatting.DARK_PURPLE.getColor());
        put(EXTRA, JSSTElementBuilder.ui(Items.DIAMOND_SWORD).setName(Component.translatable("jsst.itemEditor.colour.weaponStats")).asStack(), ChatFormatting.DARK_GREEN.getColor());

        gradient(JSSTElementBuilder.ui(Items.APPLE).setName(Component.translatable("jsst.itemEditor.gradient.rainbow")).asStack(), Gradients.RAINBOW);
        gradient(JSSTElementBuilder.ui(Items.AMETHYST_BLOCK).setName(Component.translatable("jsst.itemEditor.gradient.vaporwave")).asStack(), Gradient.linear(
                Colour.fromRGB(255, 0, 255),
                Colour.fromRGB(0, 255, 255),
                Gradient.LinearMode.RGB));
        gradient(JSSTElementBuilder.ui(Items.GLOWSTONE).setName(Component.translatable("jsst.itemEditor.gradient.solar")).asStack(), Gradient.of(
                Colour.fromRGB(40, 23, 0),
                red.jackf.jackfredlib.api.colour.Colours.YELLOW,
                red.jackf.jackfredlib.api.colour.Colours.ORANGE,
                red.jackf.jackfredlib.api.colour.Colours.WHITE
        ));
        gradient(JSSTElementBuilder.ui(Items.MUSIC_DISC_FAR).setName(Component.translatable("jsst.itemEditor.gradient.far")).asStack(), Gradient.linear(
                Colour.fromRGB(122, 221, 107),
                Colour.fromRGB(254, 180, 61),
                Gradient.LinearMode.HSV_SHORT
        ));

        gradient(JSSTElementBuilder.ui(Items.PRISMARINE_SHARD).setName(Component.translatable("jsst.itemEditor.gradient.gay")).asStack(), Gradients.GAY);
        gradient(JSSTElementBuilder.ui(Items.CAT_SPAWN_EGG).setName(Component.translatable("jsst.itemEditor.gradient.lesbian")).asStack(), Gradients.LESBIAN);
        gradient(JSSTElementBuilder.ui(Items.TOTEM_OF_UNDYING).setName(Component.translatable("jsst.itemEditor.gradient.bisexual")).asStack(), Gradients.BISEXUAL);
        gradient(JSSTElementBuilder.ui(Items.ENDER_EYE).setName(Component.translatable("jsst.itemEditor.gradient.pansexual")).asStack(), Gradients.PANSEXUAL);
        gradient(JSSTElementBuilder.ui(Items.POTION.getDefaultInstance()).setName(Component.translatable("jsst.itemEditor.gradient.intersex")).asStack(), Gradients.INTERSEX_SHARP);
        gradient(JSSTElementBuilder.ui(Items.ALLAY_SPAWN_EGG).setName(Component.translatable("jsst.itemEditor.gradient.nonbinary")).asStack(), Gradients.NONBINARY);
        gradient(JSSTElementBuilder.ui(Items.EGG).setName(Component.translatable("jsst.itemEditor.gradient.transgender")).asStack(), Gradients.TRANS);
        gradient(JSSTElementBuilder.ui(Items.BREAD).setName(Component.translatable("jsst.itemEditor.gradient.asexual")).asStack(), Gradients.ACE);
        gradient(JSSTElementBuilder.ui(Items.STRUCTURE_VOID).setName(Component.translatable("jsst.itemEditor.gradient.aromantic")).asStack(), Gradients.ARO);
    }
}
