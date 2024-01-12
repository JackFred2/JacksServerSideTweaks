package red.jackf.jsst.feature.itemeditor.previousColours;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.api.colour.GradientBuilder;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.banners.Banners;

import java.util.List;
import java.util.function.Consumer;

public interface PlayerHistoryGui {
    static void drawColours(SlotGuiInterface gui, int topLeftCol, int topLeftRow, Consumer<Colour> callback) {
        List<Colour> previous = ((EditorColourHistory) gui.getPlayer()).jsst$itemEditor$getPreviousColours();
        GridTranslator slots = GridTranslator.between(topLeftCol, topLeftCol + 4, topLeftRow, topLeftRow + 4);

        slots.fill(gui, ItemStack.EMPTY);

        for (GridTranslator.SlotItemPair<Colour> pair : slots.iterate(previous)) {
            Colour colour = pair.item();
            gui.setSlot(pair.slot(), GuiElementBuilder.from(DyeItem.byColor(colour.closestDyeColour()).getDefaultInstance())
                    .setName(Component.literal("█".repeat(8)).withColor(colour.toARGB()))
                    .addLoreLine(Util.formatAsHex(colour))
                    .addLoreLine(Hints.leftClick(Translations.select()))
                    .setCallback(Inputs.leftClick(() -> callback.accept(colour))));
        }
    }

    static void drawGradients(SlotGuiInterface gui, int topLeftCol, int topLeftRow, Consumer<Gradient> callback) {
        List<Gradient> previous = ((EditorColourHistory) gui.getPlayer()).jsst$itemEditor$getPreviousGradients();
        GridTranslator slots = GridTranslator.between(topLeftCol, topLeftCol + 4, topLeftRow, topLeftRow + 4);

        slots.fill(gui, ItemStack.EMPTY);

        for (GridTranslator.SlotItemPair<Gradient> pair : slots.iterate(previous)) {
            Gradient gradient = pair.item();

            DyeColor from = gradient.sample(0).closestDyeColour();
            DyeColor to = gradient.sample(GradientBuilder.END).closestDyeColour();

            gui.setSlot(pair.slot(), GuiElementBuilder.from(Banners.fromColours(from, to))
                    .setName(Component.translatable("jsst.itemEditor.gradient.custom"))
                    .hideFlags()
                    .addLoreLine(Util.colourise(Component.literal("|".repeat(40)), Component.empty(), gradient))
                    .addLoreLine(Hints.leftClick(Translations.select()))
                    .setCallback(Inputs.leftClick(() -> callback.accept(gradient))));
        }
    }
}
