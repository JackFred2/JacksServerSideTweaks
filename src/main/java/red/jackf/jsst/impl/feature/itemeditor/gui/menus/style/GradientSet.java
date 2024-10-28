package red.jackf.jsst.impl.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.UIRegion;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

enum GradientSet {
    DYES(() -> JSSTElementBuilder.from(Items.RED_DYE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.dyes")).build(), makeDyes()),
    CHAT_FORMATTING(() -> JSSTElementBuilder.from(Items.DARK_OAK_SIGN).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.chatFormatting")).build(), makeChatFormatting()),
    MISC(() -> JSSTElementBuilder.from(Items.APPLE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.misc")).build(), makeMiscellaneous()),
    PLAYER_HISTORY(() -> JSSTElementBuilder.from(Items.RED_DYE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.playerColourHistory")).build(), GradientSet::drawPlayerColours);

    private final Supplier<GuiElementInterface> icon;
    private final DrawFunction drawFunction;

    GradientSet(Supplier<GuiElementInterface> icon, Map<Gradient, ItemStack> preset) {
        this(icon, (player, slots, callback) -> slots.loadElements(preset.entrySet()
                .stream()
                .map(entry -> JSSTElementBuilder.from(entry.getValue())
                        .leftClick(Translations.select(), () -> callback.accept(entry.getKey()))
                        .build())));
    }

    GradientSet(Supplier<GuiElementInterface> icon, DrawFunction drawFunction) {
        this.icon = icon;
        this.drawFunction = drawFunction;
    }

    GuiElementInterface getIcon() {
        return this.icon.get();
    }

    void draw(ServerPlayer player, UIRegion slots, Consumer<Gradient> onSelect) {
        this.drawFunction.draw(player, slots, onSelect);
    }

    private static Map<Gradient, ItemStack> makeDyes() {
        Map<Gradient, ItemStack> dyes = new LinkedHashMap<>();

        for (DyeColor dye : ColourUtils.CANON_DYE_ORDER) {
            dyes.put(Colour.fromInt(dye.getTextColor()),
                    JSSTElementBuilder.from(DyeItem.byColor(dye)).ui()
                            .hideDefaultTooltip()
                            .setName(Translations.colour(dye).withColor(dye.getTextColor()))
                            .asStack());
        }

        return dyes;
    }

    private static Map<Gradient, ItemStack> makeChatFormatting() {
        Map<Gradient, ItemStack> formats = new LinkedHashMap<>();

        for (ChatFormatting format : ChatFormatting.values()) {
            if (format.isColor()) {
                //noinspection DataFlowIssue
                formats.put(Colour.fromInt(format.getColor()),
                        JSSTElementBuilder.from(Items.LEATHER_CHESTPLATE).ui()
                                .hideDefaultTooltip()
                                .setName(Component.literal("&" + format.getChar()).withStyle(format))
                                .addLoreLine(Component.literal(format.getName()).withStyle(Styles.LABEL))
                                .setComponent(DataComponents.DYED_COLOR, new DyedItemColor(format.getColor(), false))
                                .asStack());
            }
        }

        return formats;
    }

    @SuppressWarnings("DataFlowIssue")
    private static Map<Gradient, ItemStack> makeMiscellaneous() {
        Map<Gradient, ItemStack> colours = new LinkedHashMap<>();

        // begging for local functions
        BiConsumer<Rarity, ItemLike> rarityGen = (rarity, item) -> colours.put(Colour.fromInt(rarity.color().getColor()), JSSTElementBuilder.from(item).ui()
                .hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.%sRarity".formatted(rarity.getSerializedName())).withStyle(rarity.color()))
                .asStack());

        rarityGen.accept(Rarity.COMMON, Items.APPLE);
        rarityGen.accept(Rarity.UNCOMMON, Items.CHAINMAIL_CHESTPLATE);
        rarityGen.accept(Rarity.RARE, Items.TRIDENT);
        rarityGen.accept(Rarity.EPIC, Items.HEAVY_CORE);

        colours.put(Colour.fromInt(ChatFormatting.GRAY.getColor()), JSSTElementBuilder.from(Items.IRON_CHESTPLATE).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.infoTooltips"))
                .asStack());

        colours.put(Colour.fromInt(ChatFormatting.DARK_PURPLE.getColor()), JSSTElementBuilder.from(Items.SPLASH_POTION).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.potionTooltips"))
                .asStack());

        colours.put(Colour.fromInt(ChatFormatting.DARK_GRAY.getColor()), JSSTElementBuilder.from(Items.COMMAND_BLOCK).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.debugTooltips"))
                .asStack());

        colours.put(Colour.fromInt(ChatFormatting.DARK_GREEN.getColor()), JSSTElementBuilder.from(Items.DIAMOND_AXE).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.weaponAttributes"))
                .asStack());

        colours.put(Colour.fromInt(ChatFormatting.BLUE.getColor()), JSSTElementBuilder.from(Items.GLISTERING_MELON_SLICE).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.positiveAttributes"))
                .asStack());

        colours.put(Colour.fromInt(ChatFormatting.RED.getColor()), JSSTElementBuilder.from(Items.SPIDER_EYE).hideDefaultTooltip()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.gradient.negativeAttributes"))
                .asStack());

        return colours;
    }

    private static void drawPlayerColours(ServerPlayer player, UIRegion slots, Consumer<Gradient> callback) {

    }

    private interface DrawFunction {
        void draw(ServerPlayer player, UIRegion slots, Consumer<Gradient> callback);
    }
}
