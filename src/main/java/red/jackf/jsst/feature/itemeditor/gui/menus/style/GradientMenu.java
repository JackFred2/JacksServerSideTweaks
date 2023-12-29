package red.jackf.jsst.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.*;

import java.util.function.Consumer;

public class GradientMenu extends SimpleGui {
    private final Consumer<Result<Gradient>> callback;
    private Colour first = Colour.fromHSV((float) Math.random(), 1, 1);
    private Colour second = Colour.fromHSV((float) Math.random(), 1, 1);
    private Gradient.LinearMode mode = Gradient.LinearMode.RGB;

    public GradientMenu(
            ServerPlayer player,
            Consumer<Result<Gradient>> callback) {
        super(MenuType.GENERIC_9x1, player, false);
        this.callback = callback;

        this.setTitle(Component.translatable("jsst.itemEditor.gradient.custom"));
    }

    @Override
    public void onOpen() {
        this.redraw();
    }

    @Override
    public void onClose() {
        this.callback.accept(Result.empty());
    }

    private GuiElementBuilderInterface<?> makePreview(Colour colour, Consumer<Result<Colour>> callback) {
        return GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                .setName(Component.literal("█".repeat(8)).withColor(colour.toARGB()))
                .addLoreLine(Util.formatAsHex(colour))
                .addLoreLine(Hints.leftClick(Translations.change()))
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.click(player);
                    EditorMenus.colour(player, callback);
                }));
    }

    private Gradient build() {
        if (first.equals(second)) {
            return first;
        } else {
            return Gradient.linear(first, second, mode);
        }
    }

    private void redraw() {
        this.setSlot(0, makePreview(first, result -> {
            if (result.hasResult()) first = result.result();
            this.open();
        }));

        this.setSlot(2, GuiElementBuilder.from(new ItemStack(Items.GLOWSTONE))
                .setName(Util.colourise(Component.literal("|".repeat(40)), Component.empty(), build()))
                .addLoreLine(Hints.leftClick(Translations.confirm()))
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.click(player);
                    this.callback.accept(Result.of(build()));
                })));

        this.setSlot(4, makePreview(second, result -> {
            if (result.hasResult()) second = result.result();
            this.open();
        }));

        this.setSlot(5, CommonLabels.divider());

        this.setSlot(7, GuiElementBuilder.from(Items.ENDER_PEARL.getDefaultInstance())
                .setName(Component.translatable("jsst.itemEditor.gradient.custom.swap").withStyle(Styles.INPUT_HINT))
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.click(player);
                    var temp = first;
                    first = second;
                    second = temp;
                    this.redraw();
                })));

        this.setSlot(8, CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.callback.accept(Result.empty());
        }));
    }
}
