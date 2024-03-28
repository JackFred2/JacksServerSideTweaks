package red.jackf.jsst.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.SwitchButton;

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

        this.drawStatic();
    }

    private void drawStatic() {
        this.setSlot(5, CommonLabels.divider());

        this.setSlot(6, SwitchButton.<Gradient.LinearMode>builder(Component.translatable("jsst.itemEditor.gradient.custom.mode"))
                                    .addOption(Gradient.LinearMode.RGB, JSSTElementBuilder.ui(Items.RED_CONCRETE)
                                            .setName(Component.translatable("jsst.itemEditor.gradient.custom.mode.rgb"))
                                            .dontCleanText()
                                            .asStack())
                                    .addOption(Gradient.LinearMode.HSV_SHORT, JSSTElementBuilder.ui(Items.LIME_CONCRETE)
                                            .setName(Component.translatable("jsst.itemEditor.gradient.custom.mode.hsv_short"))
                                            .dontCleanText()
                                            .asStack())
                                    .addOption(Gradient.LinearMode.HSV_LONG, JSSTElementBuilder.ui(Items.LIGHT_BLUE_CONCRETE)
                                            .setName(Component.translatable("jsst.itemEditor.gradient.custom.mode.hsv_long"))
                                            .dontCleanText()
                                            .asStack())
                                    .setCallback(mode -> {
                                        this.mode = mode;
                                        this.redraw();
                                    }).build(mode));

        this.setSlot(7, JSSTElementBuilder.ui(Items.ENDER_PEARL)
                        .leftClick(Component.translatable("jsst.itemEditor.gradient.custom.swap"), () -> {
                                             Sounds.click(player);
                                             var temp = first;
                                             first = second;
                                             second = temp;
                                             this.redraw();
                                         }));

        this.setSlot(8, CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.callback.accept(Result.empty());
        }));
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
        return JSSTElementBuilder.ui(Items.PAPER)
                .setName(Component.literal("█".repeat(8)).withColor(colour.toARGB()))
                .addLoreLine(Util.formatAsHex(colour))
                .leftClick(Translations.change(), () -> {
                    Sounds.click(player);
                    EditorMenus.colour(player, callback);
                });
    }

    private Gradient build() {
        return Gradient.linear(first, second, mode);
    }

    private void redraw() {
        this.setSlot(0, makePreview(first, result -> {
            if (result.hasResult()) first = result.result();
            this.open();
        }));

        this.setSlot(2, JSSTElementBuilder.ui(Items.GLOWSTONE)
                .setName(Util.colourise(Component.literal("|".repeat(40)), Component.empty(), build()))
                        .leftClick(Translations.confirm(), () -> {
                    Sounds.click(player);
                    this.callback.accept(Result.of(build()));
                }));

        this.setSlot(4, makePreview(second, result -> {
            if (result.hasResult()) second = result.result();
            this.open();
        }));

        this.setTitle(Util.colourise(Component.translatable("jsst.itemEditor.gradient.custom"), Component.empty().withStyle(ChatFormatting.UNDERLINE), build()));
    }
}
