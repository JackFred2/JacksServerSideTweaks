package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;
import static red.jackf.jsst.features.itemeditor.menus.ColourMenu.COLOURS;

public class StyleMenu {
    private final ServerPlayer player;
    private final Component original;
    private final Consumer<Component> callback;
    private final String text;
    private ColourApplicator colour;
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

        var colourIndex = 0;
        for (var entry : COLOURS.entrySet()) {
            var slot = colourIndex % 4 + ((colourIndex / 4) * 9);
            elements.put(slot, new ItemGuiElement(entry.getKey(), () -> {
                Sounds.interact(player);
                this.colour = new SingleColour(TextColor.fromRgb(entry.getValue()));
                open();
            }));
            colourIndex++;
        }

        elements.put(4, new ItemGuiElement(Labels.create(Items.PAPER).withName("With Hex Code").build(), () -> {
            Sounds.interact(player);
            Menus.string(player, "#", CancellableCallback.of(hex -> {
                var parsed = TextColor.parseColor(hex);
                if (parsed != null) {
                    Sounds.success(player);
                    colour = new SingleColour(parsed);
                } else {
                    Sounds.error(player);
                }
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        var rainbow = new GradientColour(new Gradient(Colour.fromRgb(255, 0, 0), Colour.fromRgb(255, 0, 0), Gradient.Mode.HSV_LONG));
        elements.put(13, new ItemGuiElement(Labels.create(Items.REDSTONE).withName(rainbow.set(literal("Rainbow"), Labels.CLEAN)).build(), () -> {
            Sounds.interact(player);
            colour = rainbow;
            open();
        }));
        elements.put(22, new ItemGuiElement(Labels.create(Items.GLOWSTONE_DUST).withName("Custom Gradient").build(), () -> {
            Sounds.interact(player);
            Menus.gradient(player, colour instanceof GradientColour gradCol ? gradCol.gradient : rainbow.gradient, CancellableCallback.of(g -> {
                Sounds.success(player);
                colour = new GradientColour(g);
                open();
            }, () -> {
                Sounds.error(player);
                open();
            }));
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
        elements.put(16, new ItemGuiElement(Labels.create(Items.STRUCTURE_VOID).withName("Strikethrough").addStyle(Style.EMPTY.withStrikethrough(true)).build(), () -> {
            Sounds.interact(player);
            strikethrough = !strikethrough;
            open();
        }));
        elements.put(17, new ItemGuiElement(Labels.create(Items.SUSPICIOUS_STEW).withName("Obfuscated").withHint("Obfuscated").addStyle(Style.EMPTY.withObfuscated(true)).build(), () -> {
            Sounds.interact(player);
            obfuscated = !obfuscated;
            open();
        }));

        elements.put(35, new ItemGuiElement(Labels.create(Items.NAUTILUS_SHELL).withName("Reset").build(), () -> {
            Sounds.clear(player);
            loadFrom(original.getStyle());
            open();
        }));
        elements.put(44, new ItemGuiElement(Labels.create(Items.WATER_BUCKET).withName("Remove Style").build(), () -> {
            Sounds.clear(player);
            bold = italic = underline = strikethrough = obfuscated = false;
            colour = new SingleColour(TextColor.fromLegacyFormat(ChatFormatting.WHITE));
            open();
        }));
        elements.put(51, new ItemGuiElement(Labels.create(Items.WRITTEN_BOOK).withName(build()).withHint("Click to confirm").build(), () -> {
            Sounds.success(player);
            callback.accept(build());
        }));
        elements.put(53, EditorUtils.cancel(() -> {
            Sounds.error(player);
            callback.accept(original);
        }));

        player.openMenu(EditorUtils.make9x6(literal("Editing Style"), elements));
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

    interface ColourApplicator {
        MutableComponent set(MutableComponent in, Style style);
    }

    record SingleColour(TextColor base) implements ColourApplicator {
        @Override
        public MutableComponent set(MutableComponent in, Style style) {
            return in.withStyle(style.withColor(base));
        }
    }

    record GradientColour(Gradient gradient) implements ColourApplicator {
        @Override
        public MutableComponent set(MutableComponent in, Style style) {
            var base = literal("");
            var str = in.getString();
            for (int i = 0; i < str.length(); i++) {
                base.append(literal(String.valueOf(str.charAt(i))).setStyle(gradient.evaluate((float) i / (str.length())).style().applyTo(Labels.CLEAN)));
            }
            return base;
        }
    }
}
