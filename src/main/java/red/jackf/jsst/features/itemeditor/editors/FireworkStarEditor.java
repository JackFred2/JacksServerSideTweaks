package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.*;
import java.util.function.Consumer;

public class FireworkStarEditor extends Editor {
    private FireworkType fireworkType = FireworkType.SMALL_BALL;
    private TrailToggle hasTrail = TrailToggle.NO_TRAIL;
    private TwinkleToggle hasTwinkle = TwinkleToggle.NO_TWINKLE;
    private static final int MAX_COLOURS = 16;
    private final List<Colour> colours = new ArrayList<>();
    private final List<Colour> fadeColours = new ArrayList<>();
    private ColourView colourView = ColourView.COLOURS;

    public FireworkStarEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        parseStar();
    }

    private void parseStar() {
        var tag = stack.getTagElement(FireworkRocketItem.TAG_EXPLOSION);
        if (tag == null) return;
        this.fireworkType = BY_SHAPE.get(FireworkRocketItem.Shape.byId(tag.getInt(FireworkRocketItem.TAG_EXPLOSION_TYPE)));
        this.hasTrail = tag.getBoolean(FireworkRocketItem.TAG_EXPLOSION_TRAIL) ? TrailToggle.TRAIL : TrailToggle.NO_TRAIL;
        this.hasTwinkle = tag.getBoolean(FireworkRocketItem.TAG_EXPLOSION_FLICKER) ? TwinkleToggle.TWINKLE : TwinkleToggle.NO_TWINKLE;
        this.colours.clear();
        for (int intCol : tag.getIntArray(FireworkRocketItem.TAG_EXPLOSION_COLORS)) {
            this.colours.add(new Colour(intCol));
        }
        this.fadeColours.clear();
        for (int intCol : tag.getIntArray(FireworkRocketItem.TAG_EXPLOSION_FADECOLORS)) {
            this.fadeColours.add(new Colour(intCol));
        }
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.FIREWORK_STAR).withName("Edit Firework Star").build();
    }

    @Override
    public boolean applies(ItemStack stack) {
        return stack.is(Items.FIREWORK_STAR);
    }

    private ItemStack build() {
        var explosion = new CompoundTag();
        if (this.hasTwinkle == TwinkleToggle.TWINKLE) explosion.putBoolean(FireworkRocketItem.TAG_EXPLOSION_FLICKER, true);
        if (this.hasTrail == TrailToggle.TRAIL) explosion.putBoolean(FireworkRocketItem.TAG_EXPLOSION_TRAIL, true);
        explosion.putInt(FireworkRocketItem.TAG_EXPLOSION_TYPE, this.fireworkType.shape.getId());
        var colours = this.colours.size() > 0 ? this.colours.stream().map(Colour::value).toList() : List.of(DyeColor.BLACK.getFireworkColor());
        explosion.putIntArray(FireworkRocketItem.TAG_EXPLOSION_COLORS, colours);
        if (this.fadeColours.size() > 0) explosion.putIntArray(FireworkRocketItem.TAG_EXPLOSION_FADECOLORS, this.fadeColours.stream().map(Colour::value).toList());
        var stack = getOriginal();
        stack.getOrCreateTag().put(FireworkRocketItem.TAG_EXPLOSION, explosion);
        return stack;
    }

    private static int getSlot(int i) {
        return (i / 4) * 9 + 5 + (i % 4);
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(10, new ItemGuiElement(Labels.create(build()).keepLore().withHint("Click to finish").build(), () -> {
            this.stack = build();
            complete();
        }));

        elements.put(3, Selector.create(ColourView.class, "Viewing:", this.colourView, newType -> {
            Sounds.interact(player);
            this.colourView = newType;
            open();
        }));

        elements.put(27, Selector.create(FireworkType.class, "Firework Shape", this.fireworkType, newType -> {
            Sounds.interact(player);
            this.fireworkType = newType;
            open();
        }));

        elements.put(28, Selector.create(TrailToggle.class, "Trail Toggle", this.hasTrail, newType -> {
            Sounds.interact(player);
            this.hasTrail = newType;
            open();
        }));

        elements.put(29, Selector.create(TwinkleToggle.class, "Twinkle Toggle", this.hasTwinkle, newType -> {
            Sounds.interact(player);
            this.hasTwinkle = newType;
            open();
        }));

        for (int i = 4; i < 36; i += 9) elements.put(i, EditorUtils.divider());

        int i;
        var list = colourView == ColourView.COLOURS ? this.colours : this.fadeColours;
        for (i = 0; i < Math.min(MAX_COLOURS, list.size() + 1); i++) {
            var slot = getSlot(i);
            if (i < list.size()) {
                var colour = list.get(i);
                var dyeColour = DyeColor.byFireworkColor(colour.value());
                var title = Component.literal(colour.formatString());
                int finalI = i;
                elements.put(slot, new ItemGuiElement(Labels.create(EditorUtils.colourToItem(colour.value()))
                        .withName(title.withStyle(Style.EMPTY.withItalic(false).withColor(colour.value()))).withHint("Click to change/remove").build(), () -> {
                    Sounds.interact(player);
                    Menus.removeableColour(player, CancellableCallback.of(opt -> {
                        if (opt.isPresent()) {
                            Sounds.success(player);
                            list.set(finalI, new Colour(opt.get().value()));
                        } else {
                            Sounds.error(player);
                            list.remove(finalI);
                        }
                        open();
                    }, () -> {
                        Sounds.error(player);
                        open();
                    }));
                }));
            } else {
                elements.put(slot, new ItemGuiElement(Labels.create(Items.NETHER_STAR).withName("Add new colour").build(), () -> {
                Sounds.success(player);
                list.add(Colour.fromHsv(new Random().nextFloat(), 1, 1));
                open();
            }));
            }
        }

        elements.put(30, EditorUtils.cancel(this::cancel));

        player.openMenu(EditorUtils.make9x4(Component.literal("Editing Firework Star"), elements));
    }

    public enum ColourView implements Selector.Labeled {
        COLOURS(Labels.create(Items.NETHER_STAR).build(), "Colours"),
        FADE_COLOURS(Labels.create(Items.BLAZE_POWDER).build(), "Fade Colours");

        private final ItemStack label;
        private final String settingName;

        ColourView(ItemStack label, String settingName) {
            this.label = label;
            this.settingName = settingName;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    private static final Map<FireworkRocketItem.Shape, FireworkType> BY_SHAPE = new HashMap<>(FireworkRocketItem.Shape.values().length);

    public enum FireworkType implements Selector.Labeled {
        SMALL_BALL(FireworkRocketItem.Shape.SMALL_BALL, Labels.create(Items.GLASS).build(), "Small Ball"),
        LARGE_BALL(FireworkRocketItem.Shape.LARGE_BALL, Labels.create(Items.FIRE_CHARGE).build(), "Large Ball"),
        STAR(FireworkRocketItem.Shape.STAR, Labels.create(Items.GOLD_NUGGET).build(), "Star"),
        CREEPER(FireworkRocketItem.Shape.CREEPER, Labels.create(Items.CREEPER_HEAD).build(), "Creeper-shaped"),
        BURST(FireworkRocketItem.Shape.BURST, Labels.create(Items.FEATHER).build(), "Burst");

        private final FireworkRocketItem.Shape shape;
        private final ItemStack label;
        private final String settingName;

        FireworkType(FireworkRocketItem.Shape shape, ItemStack label, String settingName) {
            this.shape = shape;
            this.label = label;
            this.settingName = settingName;
            BY_SHAPE.put(shape, this);
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    public enum TrailToggle implements Selector.Labeled {
        NO_TRAIL(Labels.create(Items.COAL).build(), "No Trail"),
        TRAIL(Labels.create(Items.DIAMOND).build(), "With Trail");

        private final ItemStack label;
        private final String settingName;

        TrailToggle(ItemStack label, String settingName) {
            this.label = label;
            this.settingName = settingName;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    public enum TwinkleToggle implements Selector.Labeled {
        NO_TWINKLE(Labels.create(Items.GUNPOWDER).build(), "No Twinkle"),
        TWINKLE(Labels.create(Items.GLOWSTONE_DUST).build(), "With Twinkle");

        private final ItemStack label;
        private final String settingName;

        TwinkleToggle(ItemStack label, String settingName) {
            this.label = label;
            this.settingName = settingName;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }
}
