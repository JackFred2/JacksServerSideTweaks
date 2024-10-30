package red.jackf.jsst.impl.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.function.TriConsumer;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.Gradients;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.TextUtils;
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
    DYES(() -> JSSTElementBuilder.from(Items.RED_DYE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.page.dyeColours")).build(), makeDyes()),
    CHAT_FORMATTING(() -> JSSTElementBuilder.from(Items.DARK_OAK_SIGN).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.page.chatFormatting")).build(), makeChatFormatting()),
    MISC(() -> JSSTElementBuilder.from(Items.APPLE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.page.misc")).build(), makeMiscellaneous()),
    GRADIENTS(() -> JSSTElementBuilder.from(Items.GOLDEN_APPLE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.page.gradients")).build(), makeGradients()),
    PLAYER_HISTORY(() -> JSSTElementBuilder.from(Items.RED_DYE).ui().setName(Component.translatable("jsst.itemEditor.changeStyle.page.playerColours")).build(), GradientSet::drawPlayerColours);

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
                .setName(Component.translatable("jsst.itemEditor.changeStyle.page.misc.%sRarity".formatted(rarity.getSerializedName())).withStyle(rarity.color()))
                .asStack());

        rarityGen.accept(Rarity.COMMON, Items.APPLE);
        rarityGen.accept(Rarity.UNCOMMON, Items.CHAINMAIL_CHESTPLATE);
        rarityGen.accept(Rarity.RARE, Items.TRIDENT);
        rarityGen.accept(Rarity.EPIC, Items.HEAVY_CORE);

        TriConsumer<ChatFormatting, ItemLike, String> miscGen = (format, item, key) -> colours.put(Colour.fromInt(format.getColor()), JSSTElementBuilder.from(item).ui()
                .hideDefaultTooltip()
                .setName(Component.translatable(key).withColor(format.getColor()))
                .asStack());

        miscGen.accept(ChatFormatting.GRAY, Items.IRON_CHESTPLATE, "jsst.itemEditor.changeStyle.page.misc.infoTooltips");
        miscGen.accept(ChatFormatting.DARK_PURPLE, Items.SPLASH_POTION, "jsst.itemEditor.changeStyle.page.misc.potionTooltips");
        miscGen.accept(ChatFormatting.DARK_GRAY, Items.COMMAND_BLOCK, "jsst.itemEditor.changeStyle.page.misc.debugTooltips");
        miscGen.accept(ChatFormatting.DARK_GREEN, Items.DIAMOND_AXE, "jsst.itemEditor.changeStyle.page.misc.weaponAttributes");
        miscGen.accept(ChatFormatting.BLUE, Items.GLISTERING_MELON_SLICE, "jsst.itemEditor.changeStyle.page.misc.positiveAttributes");
        miscGen.accept(ChatFormatting.RED, Items.SPIDER_EYE, "jsst.itemEditor.changeStyle.page.misc.negativeAttributes");
        miscGen.accept(ChatFormatting.DARK_PURPLE, Items.WRITTEN_BOOK, "jsst.itemEditor.changeStyle.page.misc.defaultLore");

        return colours;
    }

    private static Map<Gradient, ItemStack> makeGradients() {
        Map<Gradient, ItemStack> formats = new LinkedHashMap<>();

        formats.put(Gradients.RAINBOW, JSSTElementBuilder.from(Items.RED_WOOL)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.rainbow"))
                .addLoreLine(TextUtils.previewGradient(Gradients.RAINBOW))
                .asStack());

        formats.put(Gradients.GAY, JSSTElementBuilder.from(Items.PRISMARINE_SHARD)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.gay"))
                .addLoreLine(TextUtils.previewGradient(Gradients.GAY))
                .asStack());

        formats.put(Gradients.LESBIAN, JSSTElementBuilder.from(Items.OCELOT_SPAWN_EGG)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.lesbian"))
                .addLoreLine(TextUtils.previewGradient(Gradients.LESBIAN))
                .asStack());

        formats.put(Gradients.BISEXUAL, JSSTElementBuilder.from(Items.BRICKS)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.bisexual"))
                .addLoreLine(TextUtils.previewGradient(Gradients.BISEXUAL))
                .asStack());

        formats.put(Gradients.TRANS, JSSTElementBuilder.from(Items.EGG)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.trans"))
                .addLoreLine(TextUtils.previewGradient(Gradients.TRANS))
                .asStack());

        formats.put(Gradients.PANSEXUAL, JSSTElementBuilder.from(Items.ENDER_EYE)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.pansexual"))
                .addLoreLine(TextUtils.previewGradient(Gradients.PANSEXUAL))
                .asStack());

        formats.put(Gradients.INTERSEX_SMOOTH, JSSTElementBuilder.from(Items.PURPLE_CANDLE)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.intersex"))
                .addLoreLine(TextUtils.previewGradient(Gradients.INTERSEX_SMOOTH))
                .asStack());

        formats.put(Gradients.NONBINARY, JSSTElementBuilder.from(Items.WIND_CHARGE)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.nonbinary"))
                .addLoreLine(TextUtils.previewGradient(Gradients.NONBINARY))
                .asStack());

        formats.put(Gradients.ARO, JSSTElementBuilder.from(Items.BREAD)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.aro"))
                .addLoreLine(TextUtils.previewGradient(Gradients.ARO))
                .asStack());

        formats.put(Gradients.ACE, JSSTElementBuilder.from(Items.ENDER_PEARL)
                .setName(Component.translatable("jsst.itemeditor.changeStyle.page.gradients.ace"))
                .addLoreLine(TextUtils.previewGradient(Gradients.ACE))
                .asStack());

        return formats;
    }

    private static void drawPlayerColours(ServerPlayer player, UIRegion slots, Consumer<Gradient> callback) {

    }

    private interface DrawFunction {
        void draw(ServerPlayer player, UIRegion slots, Consumer<Gradient> callback);
    }
}
