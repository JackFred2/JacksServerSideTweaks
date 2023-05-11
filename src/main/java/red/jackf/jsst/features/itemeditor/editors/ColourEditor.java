package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatterns;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import static red.jackf.jsst.features.itemeditor.menus.ColourMenu.DYES;

public class ColourEditor extends Editor {
    private final DyeableLeatherItem item;
    private MergeFactor mergeFactor = MergeFactor.ONE_HUNDRED;

    public ColourEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        this.item = ((DyeableLeatherItem) stack.getItem());
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem;
    }

    @Override
    public ItemStack label() {
        var stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        if (stack.getItem() instanceof DyeableLeatherItem dyeable) dyeable.setColor(stack, Colour.fromHsv(new Random().nextFloat(), 1f, 1f).value());
        return Labels.create(stack).withName("Colour Editor").build();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        var colourIndex = 0;
        for (var entry : DYES.entrySet()) {
            var slot = colourIndex % 4 + ((colourIndex / 4) * 9);
            var colours = entry.getValue().getTextureDiffuseColors();
            elements.put(slot, new ItemGuiElement(entry.getKey(), () -> {
                Sounds.interact(player);
                Colour newColour;
                if (mergeFactor == MergeFactor.OVERWRITE) {
                    newColour = Colour.fromRgb((int) (255f * colours[0]), (int) (255f * colours[1]), (int) (255f * colours[2]));
                } else {
                    var colour = new Colour(item.getColor(stack));
                    newColour = Colour.fromRgb(
                            (int) ((colour.r() + (colours[0] * 255f * mergeFactor.value)) / (1f + mergeFactor.value)),
                            (int) ((colour.g() + (colours[1] * 255f * mergeFactor.value)) / (1f + mergeFactor.value)),
                            (int) ((colour.b() + (colours[2] * 255f * mergeFactor.value)) / (1f + mergeFactor.value))
                    );
                }
                item.setColor(stack, newColour.value());
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
                    item.setColor(stack, parsed.getValue());
                    open();
                } else {
                    Sounds.error(player);
                    open();
                }
            }, () -> {
                Sounds.error(player);
                open();
            }));
        }));

        elements.put(31, Selector.create(MergeFactor.class, "Colour Merging Factor", mergeFactor, newFactor -> {
            Sounds.interact(player);
            mergeFactor = newFactor;
            open();
        }));

        elements.put(33, new ItemGuiElement(Labels.create(stack)
                .withHint(item.hasCustomColor(stack) ? "#%x".formatted(item.getColor(stack)) : "Not dyed")
                .withHint("Click to finish")
                .build(), this::complete));

        elements.put(17, EditorUtils.clear(() -> {
            Sounds.clear(player);
            item.clearColor(stack);
            open();
        }));

        elements.put(26, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.stack = getOriginal();
            open();
        }));
        elements.put(35, EditorUtils.cancel(this::cancel));

        player.openMenu(EditorUtils.make9x4(Component.literal("Editing Colour"), elements));
    }

    public enum MergeFactor implements Selector.Labeled {
        OVERWRITE(BannerUtils.builder(DyeColor.BLACK)
                .add(BannerPatterns.CROSS, DyeColor.LIGHT_BLUE)
                .add(BannerPatterns.BORDER, DyeColor.WHITE)
                .build(), "Overwrite", -0.5f),
        ONE_HUNDRED(BannerUtils.builder(DyeColor.LIME)
                .add(BannerPatterns.BORDER, DyeColor.BLACK)
                .build(), "100%", 1f),
        FIFTY(BannerUtils.builder(DyeColor.BLACK)
                .add(BannerPatterns.HALF_HORIZONTAL_MIRROR, DyeColor.YELLOW)
                .add(BannerPatterns.BORDER, DyeColor.BLACK)
                .build(), "50%", 0.5f),
        TWENTY_FIVE(BannerUtils.builder(DyeColor.BLACK)
                .add(BannerPatterns.STRIPE_BOTTOM, DyeColor.ORANGE)
                .add(BannerPatterns.BORDER, DyeColor.BLACK)
                .build(), "25%", 0.25f),
        TEN(BannerUtils.builder(DyeColor.BLACK)
                .add(BannerPatterns.TRIANGLES_BOTTOM, DyeColor.RED)
                .add(BannerPatterns.BORDER, DyeColor.BLACK)
                .build(), "10%", 0.1f);

        private final ItemStack label;
        private final String name;
        private final float value;

        MergeFactor(ItemStack stack, String name, float value) {
            this.label = Labels.create(stack).build();
            this.name = name;
            this.value = value;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return name;
        }

        public float getValue() {
            return value;
        }
    }
}
