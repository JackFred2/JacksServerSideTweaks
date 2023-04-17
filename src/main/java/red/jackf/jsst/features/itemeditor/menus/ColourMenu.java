package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.minecraft.network.chat.Component.translatable;

public class ColourMenu {
    public static Component colourName(String text) {
        return translatable("color.minecraft." + text);
    }
    public static final Map<ItemStack, Integer> COLOURS = new LinkedHashMap<>();
    private static void put(ItemStack stack, Integer colour) {
        if (colour != null) stack.setHoverName(stack.getHoverName().copy().withStyle(Style.EMPTY.withColor(colour).withItalic(false)));
        COLOURS.put(stack, colour);
    }
    static {
        put(Labels.create(Items.WHITE_DYE).withName(colourName("white")).build(), ChatFormatting.WHITE.getColor());
        put(Labels.create(Items.LIGHT_GRAY_DYE).withName(colourName("light_gray")).withHint("Enchantment Tooltips").build(), ChatFormatting.GRAY.getColor());
        put(Labels.create(Items.GRAY_DYE).withName(colourName("gray")).build(), ChatFormatting.DARK_GRAY.getColor());
        put(Labels.create(Items.BLACK_DYE).withName(colourName("black")).build(), ChatFormatting.BLACK.getColor());
        put(Labels.create(Items.BROWN_DYE).withName(colourName("brown")).build(), DyeColor.BROWN.getTextColor());
        put(Labels.create(Items.RED_DYE).withName(colourName("red")).build(), DyeColor.RED.getTextColor());
        put(Labels.create(Items.ORANGE_DYE).withName(colourName("orange")).build(), DyeColor.ORANGE.getTextColor());
        put(Labels.create(Items.YELLOW_DYE).withName(colourName("yellow")).build(), DyeColor.YELLOW.getTextColor());
        put(Labels.create(Items.LIME_DYE).withName(colourName("lime")).build(), DyeColor.LIME.getTextColor());
        put(Labels.create(Items.GREEN_DYE).withName(colourName("green")).build(), DyeColor.GREEN.getTextColor());
        put(Labels.create(Items.CYAN_DYE).withName(colourName("cyan")).build(), DyeColor.CYAN.getTextColor());
        put(Labels.create(Items.LIGHT_BLUE_DYE).withName(colourName("light_blue")).build(), 0x00B0FF);
        put(Labels.create(Items.BLUE_DYE).withName(colourName("blue")).build(), DyeColor.BLUE.getTextColor());
        put(Labels.create(Items.PURPLE_DYE).withName(colourName("purple")).build(), DyeColor.PURPLE.getTextColor());
        put(Labels.create(Items.MAGENTA_DYE).withName(colourName("magenta")).build(), DyeColor.MAGENTA.getTextColor());
        put(Labels.create(Items.PINK_DYE).withName(colourName("pink")).build(), 0xFF8EC6);
        put(Labels.create(Items.APPLE).withName("Common").build(), ChatFormatting.WHITE.getColor());
        put(Labels.create(Items.EXPERIENCE_BOTTLE).withName("Uncommon").build(), ChatFormatting.YELLOW.getColor());
        put(Labels.create(Items.GOLDEN_APPLE).withName("Rare").build(), ChatFormatting.AQUA.getColor());
        put(Labels.create(Items.ENCHANTED_GOLDEN_APPLE).withName("Epic").build(), ChatFormatting.LIGHT_PURPLE.getColor());
        put(Labels.create(Items.NAME_TAG).withName("Tooltip").build(), ChatFormatting.BLUE.getColor());
    }

    private final ServerPlayer player;
    private final CancellableCallback<Colour> callback;

    protected ColourMenu(ServerPlayer player, CancellableCallback<Colour> callback) {
        this.player = player;
        this.callback = callback;
    }

    protected void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        var slot = 0;
        for (var colour : COLOURS.entrySet())
            elements.put(slot++, new ItemGuiElement(colour.getKey(), () -> callback.accept(new Colour(colour.getValue()))));

        elements.put(25, new ItemGuiElement(Labels.create(Items.PAPER).withName("With Hex Code").build(), () -> {
            Sounds.interact(player);
            Menus.string(player, "#", CancellableCallback.of(hex -> {
                var parsed = TextColor.parseColor(hex);
                if (parsed != null) {
                    callback.accept(new Colour(parsed.getValue()));
                } else {
                    Sounds.error(player);
                    open();
                }
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));
        elements.put(26, EditorUtils.cancel(callback::cancel));

        player.openMenu(EditorUtils.make9x3(Component.literal("Select a colour"), elements));
    }
}
