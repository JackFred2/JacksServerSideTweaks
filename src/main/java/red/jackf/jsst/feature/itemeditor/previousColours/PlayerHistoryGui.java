package red.jackf.jsst.feature.itemeditor.previousColours;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Inputs;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;

import java.util.List;
import java.util.function.Consumer;

public interface PlayerHistoryGui {
    static void drawColours(SlotGuiInterface gui, int topLeftCol, int topLeftRow, Consumer<Colour> callback) {
        List<Colour> previous = ((EditorColourHistory) gui.getPlayer()).jsst$itemEditor$getPreviousColours();
        Util.SlotTranslator slots = Util.slotTranslator(topLeftCol, topLeftCol + 4, topLeftRow, topLeftRow + 4);

        slots.fill(gui, ItemStack.EMPTY);

        for (Util.SlotTranslator.SlotItemPair<Colour> pair : slots.iterate(previous)) {
            Colour colour = pair.item();
            gui.setSlot(pair.slot(), GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                                                      .setName(Component.literal("█".repeat(8)).withColor(colour.toARGB()))
                                                      .addLoreLine(Util.formatAsHex(colour))
                                                      .addLoreLine(Hints.leftClick(Translations.select()))
                                                      .setCallback(Inputs.leftClick(() -> callback.accept(colour))));
        }
    }

    static void drawGradients(SlotGuiInterface gui, int topLeftCol, int topLeftRow, Consumer<Gradient> callback) {
        List<Gradient> previous = ((EditorColourHistory) gui.getPlayer()).jsst$itemEditor$getPreviousGradients();
        Util.SlotTranslator slots = Util.slotTranslator(topLeftCol, topLeftCol + 4, topLeftRow, topLeftRow + 4);

        slots.fill(gui, ItemStack.EMPTY);

        for (Util.SlotTranslator.SlotItemPair<Gradient> pair : slots.iterate(previous)) {
            Gradient gradient = pair.item();
            gui.setSlot(pair.slot(), GuiElementBuilder.from(Items.WRITTEN_BOOK.getDefaultInstance())
                                                      .setName(Component.translatable("jsst.itemEditor.gradient.custom"))
                       .hideFlags()
                                                      .addLoreLine(Util.colourise(Component.literal("|".repeat(40)), Component.empty(), gradient))
                                                      .addLoreLine(Hints.leftClick(Translations.select()))
                                                      .setCallback(Inputs.leftClick(() -> callback.accept(gradient))));
        }}
}
