package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class StyleMenu {
    private static final List<DyeColor> DYES = List.of(
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    );

    private final ServerPlayer player;
    private final Component original;
    private final Consumer<Component> callback;
    private final String text;
    private Colour colour;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean obfuscated;

    protected StyleMenu(ServerPlayer player, Component text, Consumer<Component> callback) {
        this.player = player;
        this.callback = callback;
        this.original = text.copy();

        this.text = text.getString();
        loadFrom(text.getStyle());
    }

    private void loadFrom(Style style) {
        this.bold = style.isBold();
        this.italic = style.isItalic();
        this.underline = style.isUnderlined();
        this.strikethrough = style.isStrikethrough();
        this.obfuscated = style.isObfuscated();
        this.colour = new SingleColour(style.getColor());
    }

    void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 4; col++) {
                var dyeColour = DYES.get(row * 4 + col);
                elements.put(row * 9 + col, new ItemGuiElement(Labels.create(DyeItem.byColor(dyeColour))
                        .withName(translatable("color.minecraft." + dyeColour.getName()).withStyle(Labels.CLEAN.withColor(dyeColour.getTextColor()))).build(), () -> {
                    Sounds.interact(player);
                    this.colour = new SingleColour(TextColor.fromRgb(dyeColour.getTextColor()));
                    open();
                }));
            }

        elements.put(4, new ItemGuiElement(Labels.create(Items.PAPER).withName("With Hex Code").withHint("WIP").build(), () -> {
            Sounds.interact(player);
            Menus.string(player, "#", hex -> {
                var parsed = TextColor.parseColor(hex);
                if (parsed != null) {
                    Sounds.success(player);
                    colour = new SingleColour(parsed);
                } else {
                    Sounds.error(player);
                    colour = new SingleColour(TextColor.fromLegacyFormat(ChatFormatting.WHITE));
                }
                open();
            });
        }));
        elements.put(13, new ItemGuiElement(Labels.create(Items.REDSTONE).withName(RainbowColour.create("Rainbow", Labels.CLEAN)).build(), () -> {
            Sounds.interact(player);
            colour = new RainbowColour();
            open();
        }));
        elements.put(22, new ItemGuiElement(Labels.create(Items.GUNPOWDER).withName("Reset").build(), () -> {
            Sounds.interact(player);
            loadFrom(original.getStyle());
            open();
        }));

        elements.put(6, new ItemGuiElement(Labels.create(Items.IRON_INGOT).withName("Bold").addStyle(Style.EMPTY.withBold(true)).build(), () -> {
            Sounds.interact(player);
            bold = !bold;
            open();
        }));
        elements.put(7, new ItemGuiElement(Labels.create(Items.STICK).withName("Italics").addStyle(Style.EMPTY.withItalic(true)).build(), () -> {
            Sounds.interact(player);
            italic = !italic;
            open();
        }));
        elements.put(8, new ItemGuiElement(Labels.create(Items.HEAVY_WEIGHTED_PRESSURE_PLATE).withName("Underlined").addStyle(Style.EMPTY.withUnderlined(true)).build(), () -> {
            Sounds.interact(player);
            underline = !underline;
            open();
        }));
        elements.put(15, new ItemGuiElement(Labels.create(Items.STRUCTURE_VOID).withName("Strikethrough").addStyle(Style.EMPTY.withStrikethrough(true)).build(), () -> {
            Sounds.interact(player);
            strikethrough = !strikethrough;
            open();
        }));
        elements.put(16, new ItemGuiElement(Labels.create(Items.SUSPICIOUS_STEW).withName("Obfuscated").withHint("Obfuscated").addStyle(Style.EMPTY.withObfuscated(true)).build(), () -> {
            Sounds.interact(player);
            obfuscated = !obfuscated;
            open();
        }));
        elements.put(17, new ItemGuiElement(Labels.create(Items.WATER_BUCKET).withName("Remove Style").build(), () -> {
            Sounds.clear(player);
            bold = italic = underline = strikethrough = obfuscated = false;
            colour = new SingleColour(TextColor.fromLegacyFormat(ChatFormatting.WHITE));
            open();
        }));

        elements.put(33, new ItemGuiElement(Labels.create(Items.WRITTEN_BOOK).withName(build()).withHint("Click to confirm").build(), () -> {
            Sounds.success(player);
            callback.accept(build());
        }));
        elements.put(35, EditorUtils.cancel(() -> {
            Sounds.error(player);
            callback.accept(original);
        }));

        player.openMenu(EditorUtils.make9x4(literal("Editing Style"), elements));
    }

    private Component build() {
        return colour.set(literal(text), buildFormat());
    }

    private Style buildFormat() {
        return Style.EMPTY.withBold(bold)
                .withItalic(italic)
                .withUnderlined(underline)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated);
    }

    interface Colour {
        MutableComponent set(MutableComponent in, Style style);
    }

    record SingleColour(TextColor base) implements Colour {
        @Override
        public MutableComponent set(MutableComponent in, Style style) {
            return in.withStyle(style.withColor(base));
        }
    }

    record RainbowColour() implements Colour {
        @Override
        public MutableComponent set(MutableComponent in, Style style) {
            return create(in.getString(), style);
        }

        public static MutableComponent create(String str, Style style) {
            var base = literal("");
            for (int i = 0; i < str.length(); i++) {
                var colour = Mth.hsvToRgb(((float) i)/str.length(), 1f, 1f);
                base.append(literal(String.valueOf(str.charAt(i))).setStyle(style.withColor(colour)));
            }
            return base;
        }
    }
}
