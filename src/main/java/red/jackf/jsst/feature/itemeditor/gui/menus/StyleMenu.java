package red.jackf.jsst.feature.itemeditor.gui.menus;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.gui.Colours;
import red.jackf.jsst.feature.itemeditor.gui.elements.SwitchButton;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StyleMenu extends SimpleGui {
    private final Component initial;
    private final Consumer<Optional<Component>> onResult;

    private ColourPage page = ColourPage.DYES;

    @Nullable
    private Gradient colour = null;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean obfuscated;
    @Nullable
    private ResourceLocation font = null;

    public StyleMenu(
            ServerPlayer player,
            Component initial,
            Component title,
            Consumer<Optional<Component>> onResult) {
        super(MenuType.GENERIC_9x5, player, false);
        this.initial = initial;
        this.onResult = onResult;

        this.setTitle(title);
        this.load();
    }

    @Override
    public void onOpen() {
        this.redraw();
    }

    private static GuiElementBuilder createFormattingLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = GuiElementBuilder.from(new ItemStack(Items.WRITABLE_BOOK));
        forEachFrame.accept(builder);
        return builder;
    }

    private static AnimatedGuiElementBuilder createDyeLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = new AnimatedGuiElementBuilder()
                .setInterval(20)
                .setRandom(true);

        for (DyeColor colour : DyeColor.values()) {
            forEachFrame.accept(builder);
            builder.setItem(DyeItem.byColor(colour)).saveItemStack();
        }

        return builder;
    }

    private static GuiElementBuilder createExtraLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = GuiElementBuilder.from(new ItemStack(Items.CHORUS_FRUIT));
        forEachFrame.accept(builder);
        return builder;
    }

    private void load() {
        Style style = this.initial.getStyle();
        this.colour = style.getColor() != null ? Colour.fromInt(style.getColor().getValue()) : null;
        this.bold = style.isBold();
        this.italic = style.isItalic();
        this.underline = style.isUnderlined();
        this.strikethrough = style.isStrikethrough();
        this.obfuscated = style.isObfuscated();
        this.font = style.getFont().equals(Style.DEFAULT_FONT) ? null : style.getFont();
    }

    private GuiElementInterface createToggleButton(Item base, MutableComponent title, Supplier<Boolean> get, Consumer<Boolean> set) {
        var builder = GuiElementBuilder.from(new ItemStack(base))
                .setName(title.withStyle(get.get() ? Styles.POSITIVE : Styles.NEGATIVE))
                .addLoreLine(Hints.leftClick(Translations.toggle()))
                .hideFlags()
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.click(player);
                    set.accept(!get.get());
                    this.redraw();
                }));
        if (get.get()) builder.glow();
        return builder.build();
    }

    private void drawColourPage(Map<ItemStack, Integer> colours) {
        var colourSlots = Util.slotTranslator(0, 4, 0, 4);

        int index = 0;
        for (Map.Entry<ItemStack, Integer> entry : colours.entrySet()) {
            var slot = colourSlots.translate(index++);
            if (slot.isEmpty()) break;
            this.setSlot(slot.getAsInt(), GuiElementBuilder.from(entry.getKey())
                                                           .addLoreLine(Hints.leftClick(Translations.select()))
                                                           .setCallback(Inputs.leftClick(() -> {
                                                               Sounds.click(player);
                                                               this.colour = Colour.fromInt(entry.getValue());
                                                               this.redraw();
                                                           })));
        }
        OptionalInt slot;
        while ((slot = colourSlots.translate(index++)).isPresent()) this.clearSlot(slot.getAsInt());
    }

    private void redraw() {

        // colours
        this.page.pageDraw.accept(this);

        this.setSlot(Util.slot(3, 4), SwitchButton.create(Component.translatable("jsst.itemEditor.colour.page"),
                                                          ColourPage.class,
                                                          this.page,
                                                          newPage -> {
                                                              Sounds.click(player);
                                                              this.page = newPage;
                                                              this.redraw();
                                                          }));

        this.setSlot(Util.slot(0, 4), GuiElementBuilder.from(new ItemStack(Items.GUNPOWDER))
                .setName(Component.translatable("jsst.itemEditor.style.removeColour").withStyle(Styles.INPUT_HINT))
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.clear(player);
                    this.colour = null;
                    this.redraw();
                })));

        // styles
        this.setSlot(Util.slot(5, 0), createToggleButton(Items.IRON_BLOCK,
                                                         Component.translatable("jsst.itemEditor.style.bold").withStyle(ChatFormatting.BOLD),
                                                         () -> bold,
                                                         b -> bold = b));

        this.setSlot(Util.slot(6, 0), createToggleButton(Items.BLAZE_ROD,
                                                         Component.translatable("jsst.itemEditor.style.italic").withStyle(ChatFormatting.ITALIC),
                                                         () -> italic,
                                                         b -> italic = b));

        this.setSlot(Util.slot(7, 0), createToggleButton(Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
                                                         Component.translatable("jsst.itemEditor.style.underscore").withStyle(ChatFormatting.UNDERLINE),
                                                         () -> underline,
                                                         b -> underline = b));

        this.setSlot(Util.slot(8, 0), createToggleButton(Items.CHAIN,
                                                         Component.translatable("jsst.itemEditor.style.strikethrough").withStyle(ChatFormatting.STRIKETHROUGH),
                                                         () -> strikethrough,
                                                         b -> strikethrough = b));

        this.setSlot(Util.slot(5, 1), createToggleButton(Items.DRAGON_BREATH,
                                                         Component.empty()
                                                                  .append(Component.translatable("jsst.itemEditor.style.obfuscated").withStyle(ChatFormatting.OBFUSCATED))
                                                                  .append(Component.literal(" ("))
                                                                  .append(Component.translatable("jsst.itemEditor.style.obfuscated"))
                                                                  .append(Component.literal(")")),
                                                         () -> obfuscated,
                                                         b -> obfuscated = b));

        this.setSlot(Util.slot(6, 1), GuiElementBuilder.from(new ItemStack(Items.WRITABLE_BOOK))
                .setName(Component.empty()
                                  .append(Component.literal("Font").withStyle(Style.EMPTY.withFont(new ResourceLocation("minecraft:alt"))))
                                  .append(Component.literal(" ("))
                                  .append(Component.translatable("jsst.itemEditor.style.font"))
                                  .append(Component.literal(")")))
                .addLoreLine(Translations.current(this.font != null ? this.font.toString() : Translations.def()))
                .addLoreLine(Hints.leftClick(Translations.change()))
                .addLoreLine(Hints.rightClick(Translations.reset()))
                .setCallback(this::clickFont));

        // misc
        this.setSlot(Util.slot(8, 3), GuiElementBuilder.from(new ItemStack(Items.WATER_BUCKET))
                .setName(Component.translatable("jsst.itemEditor.style.removeStyle").withStyle(Styles.INPUT_HINT))
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(() -> {
                    Sounds.clear(player);
                    this.colour = null;
                    this.bold = this.italic = this.underline = this.strikethrough = this.obfuscated = false;
                    this.font = null;
                    this.redraw();
                })));

        this.setSlot(Util.slot(6, 4), GuiElementBuilder.from(new ItemStack(Items.WRITTEN_BOOK))
                                                       .hideFlags()
                                                       .glow()
                                                       .setName(build())
                                                       .addLoreLine(Hints.leftClick(Translations.save()))
                                                       .addLoreLine(Hints.rightClick(Translations.reset()))
                                                       .setCallback(this::clickPreview));

        this.setSlot(Util.slot(8, 4), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.onResult.accept(Optional.empty());
        }));
    }

    private void clickFont(ClickType type) {
        if (type == ClickType.MOUSE_LEFT) {
            Sounds.click(player);
            HashMap<Fonts, ItemStack> options = new HashMap<>();
            for (Fonts value : Fonts.values()) options.put(value, value.label.apply(build()));
            Menus.selector(player,
                           Component.translatable("jsst.itemEditor.style.changeFont"),
                           Arrays.asList(Fonts.values()),
                           LabelMap.createStatic(options),
                           selection -> {
                if (selection.hasResult()) {
                    if (selection.result() == Fonts.CUSTOM) {
                        Menus.resourceLocation(player,
                                               Component.translatable("jsst.itemEditor.style.changeFont"),
                                               this.font != null ? this.font : Style.DEFAULT_FONT,
                                               null,
                                               opt -> {
                                                   opt.ifPresent(resLoc -> this.font = resLoc.equals(Style.DEFAULT_FONT) ? null : resLoc);
                                                   this.open();
                                               });
                    } else {
                        this.font = selection.result().id.equals(Style.DEFAULT_FONT) ? null : selection.result().id;
                        this.open();
                    }
                } else {
                    this.open();
                }
                           });
        } else if (type == ClickType.MOUSE_RIGHT) {
            Sounds.clear(player);
            this.font = null;
            this.redraw();
        }
    }

    private void clickPreview(ClickType type) {
        if (type == ClickType.MOUSE_LEFT) {
            Sounds.click(player);
            this.onResult.accept(Optional.of(build()));
        } else if (type == ClickType.MOUSE_RIGHT) {
            Sounds.clear(player);
            this.load();
            this.redraw();
        }
    }

    private Component build() {
        Style style = buildBaseStyle();
        String str = initial.getString();
        if (this.colour == null || str.isBlank()) return Component.literal(str).withStyle(style);
        if (this.colour instanceof Colour col) return Component.literal(str).withStyle(style).withColor(col.toARGB());

        MutableComponent result = Component.empty().withStyle(style);

        final int divisor = str.length() - 1;
        for (int i = 0; i < str.length(); i++) {
            result.append(Component.literal(String.valueOf(str.charAt(i)))
                                   .withColor(this.colour.sample((float) i / divisor).toARGB()));
        }
        return result;
    }

    private Style buildBaseStyle() {
        return Style.EMPTY.withBold(bold)
                .withItalic(italic)
                .withUnderlined(underline)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
                .withFont(Objects.equals(font, Style.DEFAULT_FONT) ? null : font);
    }

    @Override
    public void onClose() {
        this.onResult.accept(Optional.empty());
    }

    private enum ColourPage implements SwitchButton.Labelled {
        DYES(StyleMenu::createDyeLabel, Component.translatable("jsst.itemEditor.colour.dyes"), menu -> menu.drawColourPage(Colours.DYES)),
        FORMATTING(StyleMenu::createFormattingLabel, Component.translatable("jsst.itemEditor.colour.chatFormatting"), menu -> menu.drawColourPage(Colours.CHAT_FORMATS)),
        EXTRA(StyleMenu::createExtraLabel, Component.translatable("jsst.itemEditor.colour.extra"), menu -> menu.drawColourPage(Colours.EXTRA));

        private final SwitchButton.LabelGetter labelSupplier;
        private final Component name;
        private final Consumer<StyleMenu> pageDraw;

        ColourPage(SwitchButton.LabelGetter labelSupplier, Component name, Consumer<StyleMenu> pageDraw) {
            this.labelSupplier = labelSupplier;
            this.name = name;
            this.pageDraw = pageDraw;
        }

        @Override
        public GuiElementBuilderInterface<?> getLabel(Consumer<GuiElementBuilderInterface<?>> applyToEachFrame) {
            return labelSupplier.get(applyToEachFrame);
        }

        @Override
        public Component getName() {
            return name;
        }
    }

    private enum Fonts {
        DEFAULT(Style.DEFAULT_FONT, new ItemStack(Items.GRASS_BLOCK)),
        ALT(new ResourceLocation("alt"), new ItemStack(Items.ENCHANTING_TABLE)),
        UNICODE(new ResourceLocation("uniform"), GuiElementBuilder.from(new ItemStack(Items.MUSIC_DISC_OTHERSIDE)).hideFlags().asStack()),
        ILLAGER(new ResourceLocation("illageralt"), Raid.getLeaderBannerInstance()),
        CUSTOM;

        private final ResourceLocation id;
        private final Function<Component, ItemStack> label;

        Fonts() {
            this.id = new ResourceLocation("jsst", "shouldntseethis");
            this.label = ignored -> GuiElementBuilder.from(new ItemStack(Items.ANVIL))
                    .setName(Component.translatable("jsst.itemEditor.style.customFont"))
                    .asStack();
        }

        Fonts(ResourceLocation id, ItemStack label) {
            this.id = id;
            this.label = text -> GuiElementBuilder.from(label)
                    .setName(Component.literal(id.toString()).withStyle(Styles.LABEL))
                    .addLoreLine(text.copy().withStyle(Style.EMPTY.withFont(id)))
                    .asStack();
        }
    }
}
