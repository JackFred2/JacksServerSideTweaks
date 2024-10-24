package red.jackf.jsst.impl.utils.sgui.menus;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.impl.utils.Callbacks;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.*;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.ToggleButton;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class StyleInputMenu extends SimpleGuiExt {
    private final Component initial;
    private final Consumer<Optional<Component>> onResult;

    @Nullable
    private Gradient colour = null;
    private boolean bold;
    private boolean italics;
    private boolean underline;
    private boolean strikethrough;
    private boolean obfuscated;

    protected StyleInputMenu(ServerPlayer player, Component text, Consumer<Optional<Component>> onResult) {
        super(MenuType.GENERIC_9x5, player, false);
        this.initial = text;
        this.onResult = Callbacks.singleUse(onResult);

        this.setTitle(Component.translatable("jsst.itemEditor.changeStyle"));
        this.loadFromStyle(this.initial.getStyle());

        this.drawStatic();
    }

    @Override
    protected void drawStatic() {
        this.setSlot(6, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.bold").withStyle(ChatFormatting.BOLD))
                .initial(this.bold)
                .disabled(JSSTElementBuilder.from(Items.GLASS).build())
                .enabled(JSSTElementBuilder.from(Items.NETHERITE_BLOCK).glow().build())
                .build(b -> {
                    Sounds.UI.click(player);
                    this.bold = b;
                    this.refresh();
                }));

        this.setSlot(7, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.italic").withStyle(ChatFormatting.ITALIC))
                .initial(this.italics)
                .disabled(JSSTElementBuilder.from(Items.LIGHTNING_ROD).build())
                .enabled(JSSTElementBuilder.from(Items.BREEZE_ROD).glow().build())
                .build(b -> {
                    Sounds.UI.click(player);
                    this.italics = b;
                    this.refresh();
                }));

        this.setSlot(8, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.underline").withStyle(ChatFormatting.UNDERLINE))
                .initial(this.underline)
                .disabled(JSSTElementBuilder.from(Items.STONE_BUTTON).build())
                .enabled(JSSTElementBuilder.from(Items.STONE_PRESSURE_PLATE).glow().build())
                .build(b -> {
                    Sounds.UI.click(player);
                    this.underline = b;
                    this.refresh();
                }));

        this.setSlot(6, 1, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.strikethrough").withStyle(ChatFormatting.STRIKETHROUGH))
                .initial(this.strikethrough)
                .disabled(JSSTElementBuilder.from(Items.IRON_NUGGET).build())
                .enabled(JSSTElementBuilder.from(Items.CHAIN).glow().build())
                .build(b -> {
                    Sounds.UI.click(player);
                    this.strikethrough = b;
                    this.refresh();
                }));

        Component obfuscated = Component.translatable("jsst.itemEditor.changeStyle.obfuscated");
        this.setSlot(7, 1, ToggleButton.builder(obfuscated.copy().withStyle(ChatFormatting.OBFUSCATED))
                .initial(this.obfuscated)
                .disabled(JSSTElementBuilder.from(Items.GLASS_BOTTLE).addLoreLine(obfuscated.copy().setStyle(Styles.LABEL)).build())
                .enabled(JSSTElementBuilder.from(Items.DRAGON_BREATH).addLoreLine(obfuscated.copy().setStyle(Styles.LABEL)).glow().build())
                .build(b -> {
                    Sounds.UI.click(player);
                    this.obfuscated = b;
                    this.refresh();
                }));

        this.setSlot(8, 3, JSSTElementBuilder.from(Items.GRINDSTONE).ui()
                .leftClick(Translations.clear(), () -> {
                    Sounds.UI.grind(player);
                    this.loadFromStyle(Style.EMPTY);
                    this.refresh();
                }));

        this.setSlot(8, 4, CommonLabels.cancel(this::cancel));
    }

    private void loadFromStyle(Style style) {
        this.colour = style.getColor() != null ? Colour.fromInt(style.getColor().getValue()) : null;
        this.bold = style.isBold();
        this.italics = style.isItalic();
        this.underline = style.isUnderlined();
        this.strikethrough = style.isStrikethrough();
        this.obfuscated = style.isObfuscated();
    }

    private Style buildStyle() {
        Style style = Style.EMPTY
                .withBold(bold)
                .withItalic(italics)
                .withUnderlined(underline)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated);

        // TODO gradient
        if (colour != null) style = style.withColor(colour.sample(0f).toARGB());

        return style;
    }

    private Component buildOutput() {
        return this.initial.copy().setStyle(buildStyle());
    }

    @Override
    protected void refresh() {
        UIRegion dyes = UIRegion.rectangle(this, 0, 0, 4, 4);

        List<DyeColor> COLOURS = List.of(
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

        List<GuiElementInterface> elements = COLOURS.stream()
                .map(col -> JSSTElementBuilder.from(DyeItem.byColor(col)).ui()
                        .setName(Translations.colour(col).withColor(col.getTextColor()))
                        .leftClick(Translations.select(), () -> {
                            Sounds.UI.click(player);
                            this.colour = Colour.fromInt(col.getTextColor());
                            this.refresh();
                        }).build()).toList();

        dyes.loadElements(elements);

        this.setSlot(6, 3, JSSTElementBuilder.from(Items.PAPER).ui()
                .setName(buildOutput())
                .leftClick(Translations.confirm(), this::complete)
                .rightClick(Translations.reset(), this::reset));
    }

    private void reset() {
        Sounds.UI.reset(player);
        this.loadFromStyle(this.initial.getStyle());
        this.refresh();
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    private void complete() {
        Sounds.UI.click(player);
        this.onResult.accept(Optional.of(buildOutput()));
    }

    private void cancel() {
        Sounds.UI.close(player);
        this.onResult.accept(Optional.empty());
    }
}
