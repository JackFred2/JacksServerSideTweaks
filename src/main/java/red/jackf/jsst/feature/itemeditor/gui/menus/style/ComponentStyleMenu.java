package red.jackf.jsst.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
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
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.feature.itemeditor.previouscolours.EditorColourHistory;
import red.jackf.jsst.feature.itemeditor.previouscolours.PlayerHistoryGui;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.SwitchButton;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ComponentStyleMenu extends SimpleGui {
    private final Component initial;
    private final Consumer<Optional<Component>> onResult;

    private Page page = Page.DYES;

    @Nullable
    private Gradient colour = null;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean obfuscated;
    @Nullable
    private ResourceLocation font = null;

    public ComponentStyleMenu(
            ServerPlayer player,
            Component initial,
            Consumer<Optional<Component>> onResult) {
        super(MenuType.GENERIC_9x5, player, false);
        this.initial = initial;
        this.onResult = onResult;

        this.setTitle(Component.translatable("jsst.itemEditor.simpleName.changeStyle"));
        this.load();
    }

    private static GuiElementInterface createGradientLabel() {
        var builder = new AnimatedGuiElementBuilder()
                .setInterval(5);

        for (DyeColor colour : List.of(DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.MAGENTA)) {
            builder.setName(Component.translatable("jsst.itemEditor.gradient.page"));
            builder.setItem(DyeItem.byColor(colour)).saveItemStack();
        }

        return builder.build();
    }

    protected static GuiElementInterface createPlayerGradientHistoryLabel(ServerPlayer player) {
        return JSSTElementBuilder.from(Items.PLAYER_HEAD)
                                .setName(Component.translatable("jsst.itemEditor.gradient.page.playerHistory"))
                                .setSkullOwner(player.getGameProfile(), null)
                .glow()
                                .build();
    }

    @Override
    public void onOpen() {
        this.redraw();
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

    private GuiElementInterface createToggleButton(
            Item base,
            MutableComponent title,
            Supplier<Boolean> get,
            Consumer<Boolean> set) {
        var builder = JSSTElementBuilder.from(new ItemStack(base))
                                       .setName(title.withStyle(get.get() ? Styles.POSITIVE : Styles.NEGATIVE))
                .hideFlags()
                .leftClick(Translations.toggle(), () -> {
                                           Sounds.click(player);
                                           set.accept(!get.get());
                                           this.redraw();
                                       });
        if (get.get()) builder.glow();
        return builder.build();
    }

    private void drawColourPage(Map<ItemStack, Colour> colours) {
        var colourSlots = GridTranslator.between(0, 4, 0, 4);
        Util.fill(this, ItemStack.EMPTY, 0, 4, 0, 4);

        int index = 0;
        for (Map.Entry<ItemStack, Colour> entry : colours.entrySet()) {
            var slot = colourSlots.translate(index++);
            if (slot.isEmpty()) break;
            this.setSlot(slot.getAsInt(), JSSTElementBuilder.from(entry.getKey())
                            .leftClick(Translations.select(), () -> {
                                                               Sounds.click(player);
                                                               this.colour = entry.getValue();
                                                               this.redraw();
                                                           }));
        }
    }

    private void drawGradients() {
        var colourSlots = GridTranslator.between(0, 4, 0, 4);
        Util.fill(this, ItemStack.EMPTY, 0, 4, 0, 4);

        int index = 0;
        for (Map.Entry<ItemStack, Gradient> entry : Colours.GRADIENTS.entrySet()) {
            var slot = colourSlots.translate(index++);
            if (slot.isEmpty()) break;
            this.setSlot(slot.getAsInt(), JSSTElementBuilder.from(entry.getKey())
                            .leftClick(Translations.select(), () -> {
                                                               Sounds.click(player);
                                                               this.colour = entry.getValue();
                                                               this.redraw();
                                                           }));
        }
    }

    private void redraw() {
        // colours
        this.page.pageDraw.accept(this);

        this.setSlot(Util.slot(0, 4), JSSTElementBuilder.ui(Items.GUNPOWDER)
                        .leftClick(Component.translatable("jsst.itemEditor.style.removeColour"), () -> {
                                                           Sounds.clear(player);
                                                           this.colour = null;
                                                           this.redraw();
                                                       }));

        this.setSlot(Util.slot(1, 4), JSSTElementBuilder.ui(Items.PAPER)
                        .leftClick(Component.translatable("jsst.itemEditor.colour.custom"), () -> {
                                                           Sounds.click(player);
                                                           Menus.customColour(player, result -> {
                                                               if (result.hasResult()) {
                                                                   ((EditorColourHistory) player).jsst$itemEditor$push(result.result());
                                                                   this.colour = result.result();
                                                               }
                                                               this.open();
                                                           });
                                                       }));

        this.setSlot(Util.slot(2, 4), JSSTElementBuilder.ui(Items.GLOWSTONE_DUST)
                                                       .setName(Component.translatable("jsst.itemEditor.gradient.custom"))
                        .leftClick(Translations.open(), () -> {
                                                           Sounds.click(player);
                                                           EditorMenus.gradient(player, result -> {
                                                               if (result.hasResult()) {
                                                                   ((EditorColourHistory) player).jsst$itemEditor$push(result.result());
                                                                   this.colour = result.result();
                                                               }
                                                               this.open();
                                                           });
                                                       }));

        this.setSlot(Util.slot(3, 4), SwitchButton.<Page>builder(Component.translatable("jsst.itemEditor.colour.page"))
                                                  .addOption(Page.DYES, ColourMenu.createDyeLabel())
                                                  .addOption(Page.FORMATTING, ColourMenu.createFormattingLabel())
                                                  .addOption(Page.EXTRA, ColourMenu.createExtraLabel())
                                                  .addOption(Page.GRADIENTS, createGradientLabel())
                                                  .addOption(Page.PLAYER_PREVIOUS_COLOURS, ColourMenu.createPlayerColourHistoryLabel(player))
                                                  .addOption(Page.PLAYER_PREVIOUS_GRADIENTS, createPlayerGradientHistoryLabel(player))
                                                  .setCallback(page -> {
                                                      this.page = page;
                                                      this.page.pageDraw.accept(this);
                                                  })
                                                  .build(this.page));

        // styles
        this.setSlot(Util.slot(5, 0), createToggleButton(Items.IRON_BLOCK,
                                                         Component.translatable("jsst.itemEditor.style.bold")
                                                                  .withStyle(ChatFormatting.BOLD),
                                                         () -> bold,
                                                         b -> bold = b));

        this.setSlot(Util.slot(6, 0), createToggleButton(Items.BLAZE_ROD,
                                                         Component.translatable("jsst.itemEditor.style.italic")
                                                                  .withStyle(ChatFormatting.ITALIC),
                                                         () -> italic,
                                                         b -> italic = b));

        this.setSlot(Util.slot(7, 0), createToggleButton(Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
                                                         Component.translatable("jsst.itemEditor.style.underscore")
                                                                  .withStyle(ChatFormatting.UNDERLINE),
                                                         () -> underline,
                                                         b -> underline = b));

        this.setSlot(Util.slot(8, 0), createToggleButton(Items.CHAIN,
                                                         Component.translatable("jsst.itemEditor.style.strikethrough")
                                                                  .withStyle(ChatFormatting.STRIKETHROUGH),
                                                         () -> strikethrough,
                                                         b -> strikethrough = b));

        this.setSlot(Util.slot(5, 1), createToggleButton(Items.DRAGON_BREATH,
                                                         Component.empty()
                                                                  .append(Component.translatable("jsst.itemEditor.style.obfuscated")
                                                                                   .withStyle(ChatFormatting.OBFUSCATED))
                                                                  .append(Component.literal(" ("))
                                                                  .append(Component.translatable("jsst.itemEditor.style.obfuscated"))
                                                                  .append(Component.literal(")")),
                                                         () -> obfuscated,
                                                         b -> obfuscated = b));

        this.setSlot(Util.slot(6, 1), JSSTElementBuilder.ui(Items.WRITABLE_BOOK)
                                                       .setName(Component.empty()
                                                                         .append(Component.literal("Font")
                                                                                          .withStyle(Style.EMPTY.withFont(new ResourceLocation("minecraft:alt"))))
                                                                         .append(Component.literal(" ("))
                                                                         .append(Component.translatable("jsst.itemEditor.style.font"))
                                                                         .append(Component.literal(")")))
                                                       .addLoreLine(Translations.current(this.font != null ? this.font.toString() : Translations.def()))
                        .leftClick(Translations.change(), this::fontSelector)
                        .rightClick(Translations.reset(), () -> {
                            Sounds.clear(player);
                            this.font = null;
                            this.redraw();
                        }));

        // misc
        this.setSlot(Util.slot(8, 3), JSSTElementBuilder.ui(Items.WATER_BUCKET)
                        .leftClick(Component.translatable("jsst.itemEditor.style.removeStyle"), () -> {
                                                           Sounds.clear(player);
                                                           this.colour = null;
                                                           this.bold = this.italic = this.underline = this.strikethrough = this.obfuscated = false;
                                                           this.font = null;
                                                           this.redraw();
                                                       }));

        this.setSlot(Util.slot(6, 4), JSSTElementBuilder.ui(Items.WRITTEN_BOOK)
                                                       .hideFlags()
                                                       .glow()
                                                       .setName(build())
                        .leftClick(Translations.save(), this::complete)
                        .rightClick(Translations.reset(), this::reset));

        this.setSlot(Util.slot(8, 4), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.onResult.accept(Optional.empty());
        }));
    }

    private void fontSelector() {
        Sounds.click(player);
        HashMap<Fonts, ItemStack> options = new HashMap<>();
        for (Fonts value : Fonts.values()) options.put(value, value.label.apply(build()));
        SelectorMenu.open(player,
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
                                                          result -> {
                                                              if (result.hasResult())
                                                                  this.font = result.result().equals(Style.DEFAULT_FONT) ? null : result.result();
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
    }

    private void setCustom(Gradient gradient) {
        Sounds.click(player);
        ((EditorColourHistory) player).jsst$itemEditor$push(gradient);
        this.colour = gradient;
        this.redraw();
    }

    private void complete() {
        Sounds.click(player);
        this.onResult.accept(Optional.of(build()));
    }

    private void reset() {
        Sounds.clear(player);
        this.load();
        this.redraw();
    }

    private Component build() {
        Style style = buildBaseStyle();
        String str = initial.getString();
        if (this.colour == null || str.isBlank()) return Component.literal(str).withStyle(style);
        if (this.colour instanceof Colour col) return Component.literal(str).withStyle(style).withColor(col.toARGB());

        return Util.colourise(initial, Component.empty().withStyle(style), this.colour);
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

    private enum Page {
        DYES(menu -> menu.drawColourPage(Colours.DYES)),
        FORMATTING(menu -> menu.drawColourPage(Colours.CHAT_FORMATS)),
        EXTRA(menu -> menu.drawColourPage(Colours.EXTRA)),
        GRADIENTS(ComponentStyleMenu::drawGradients),
        PLAYER_PREVIOUS_COLOURS(menu -> PlayerHistoryGui.drawColours(menu, 0, 0, menu::setCustom)),
        PLAYER_PREVIOUS_GRADIENTS(menu -> PlayerHistoryGui.drawGradients(menu, 0, 0, menu::setCustom));
        private final Consumer<ComponentStyleMenu> pageDraw;

        Page(Consumer<ComponentStyleMenu> pageDraw) {
            this.pageDraw = pageDraw;
        }
    }

    private enum Fonts {
        DEFAULT(Style.DEFAULT_FONT, new ItemStack(Items.GRASS_BLOCK)),
        ALT(new ResourceLocation("alt"), new ItemStack(Items.ENCHANTING_TABLE)),
        UNICODE(new ResourceLocation("uniform"), JSSTElementBuilder.from(Items.MUSIC_DISC_OTHERSIDE).hideFlags().asStack()),
        ILLAGER(new ResourceLocation("illageralt"), Raid.getLeaderBannerInstance()),
        CUSTOM;

        private final ResourceLocation id;
        private final Function<Component, ItemStack> label;

        Fonts() {
            this.id = new ResourceLocation("jsst", "shouldntseethis");
            this.label = ignored -> JSSTElementBuilder.from(Items.ANVIL).setName(Component.translatable("jsst.itemEditor.style.customFont")).asStack();
        }

        Fonts(ResourceLocation id, ItemStack label) {
            this.id = id;
            this.label = text -> JSSTElementBuilder.from(label)
                                                  .setName(Component.literal(id.toString()).withStyle(Styles.INFO))
                                                  .addLoreLine(text.copy().withStyle(Style.EMPTY.withFont(id)))
                                                  .asStack();
        }
    }
}
