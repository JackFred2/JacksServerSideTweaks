package red.jackf.jsst.impl.feature.itemeditor.gui.menus.style;

import com.mojang.serialization.DataResult;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.utils.Callbacks;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.TextUtils;
import red.jackf.jsst.impl.utils.sgui.*;
import red.jackf.jsst.impl.utils.sgui.elements.CycleButton;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.ToggleButton;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

// TODO get raw @Nullable Boolean values from style
public class StyleInputMenu extends SimpleGuiExt {
    private static final ResourceLocation DEFAULT_FONT = ResourceLocation.withDefaultNamespace("default");
    private static final ResourceLocation ALT_FONT = ResourceLocation.withDefaultNamespace("alt");
    private static final ResourceLocation UNIFORM_FONT = ResourceLocation.withDefaultNamespace("uniform");
    private static final ResourceLocation ILLAGER_FONT = ResourceLocation.withDefaultNamespace("illageralt");
    private static final ResourceLocation CUSTOM_FONT = JSST.id("custom_font");

    private final Component initial;
    private final Consumer<Optional<Component>> onResult;

    private GradientSet currentPage = GradientSet.DYES;

    @Nullable
    private Gradient colour = null;
    private boolean bold;
    private boolean italics;
    private boolean underline;
    private boolean strikethrough;
    private boolean obfuscated;
    private @Nullable ResourceLocation font;

    public StyleInputMenu(ServerPlayer player, Component text, Consumer<Optional<Component>> onResult) {
        super(MenuType.GENERIC_9x4, player, false);
        this.initial = text;
        this.onResult = Callbacks.singleUse(onResult);

        this.setTitle(Component.translatable("jsst.itemEditor.changeStyle"));
        this.loadFromStyle(this.initial.getStyle());

        this.drawStatic();
    }

    @Override
    protected void drawStatic() {
        this.setSlot(4, 0, CycleButton.<GradientSet>builder(Component.translatable("jsst.itemEditor.changeStyle.page"))
                .option(GradientSet.DYES, GradientSet.DYES.getIcon())
                .option(GradientSet.CHAT_FORMATTING, GradientSet.CHAT_FORMATTING.getIcon())
                .option(GradientSet.MISC, GradientSet.MISC.getIcon())
                .option(GradientSet.GRADIENTS, GradientSet.GRADIENTS.getIcon())
                .build(page -> {
                    Sounds.UI.click(player);
                    this.currentPage = page;
                    this.refresh();
                }));

        this.setSlot(6, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.bold")
                        .withStyle(ChatFormatting.BOLD)).initial(this.bold)
                .disabled(JSSTElementBuilder.from(Items.GLASS).build())
                .enabled(JSSTElementBuilder.from(Items.NETHERITE_BLOCK).glow().build()).build(b -> {
                    Sounds.UI.click(player);
                    this.bold = b;
                    this.refresh();
                }));

        this.setSlot(7, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.italic")
                        .withStyle(ChatFormatting.ITALIC)).initial(this.italics)
                .disabled(JSSTElementBuilder.from(Items.LIGHTNING_ROD).build())
                .enabled(JSSTElementBuilder.from(Items.BREEZE_ROD).glow().build()).build(b -> {
                    Sounds.UI.click(player);
                    this.italics = b;
                    this.refresh();
                }));

        this.setSlot(8, 0, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.underline")
                        .withStyle(ChatFormatting.UNDERLINE)).initial(this.underline)
                .disabled(JSSTElementBuilder.from(Items.STONE_BUTTON).build())
                .enabled(JSSTElementBuilder.from(Items.STONE_PRESSURE_PLATE).glow().build()).build(b -> {
                    Sounds.UI.click(player);
                    this.underline = b;
                    this.refresh();
                }));

        this.setSlot(6, 1, ToggleButton.builder(Component.translatable("jsst.itemEditor.changeStyle.strikethrough")
                        .withStyle(ChatFormatting.STRIKETHROUGH)).initial(this.strikethrough)
                .disabled(JSSTElementBuilder.from(Items.IRON_NUGGET).build())
                .enabled(JSSTElementBuilder.from(Items.CHAIN).glow().build()).build(b -> {
                    Sounds.UI.click(player);
                    this.strikethrough = b;
                    this.refresh();
                }));

        Component obfuscated = Component.translatable("jsst.itemEditor.changeStyle.obfuscated");
        this.setSlot(7, 1, ToggleButton.builder(obfuscated.copy().withStyle(ChatFormatting.OBFUSCATED))
                .initial(this.obfuscated).disabled(JSSTElementBuilder.from(Items.GLASS_BOTTLE)
                        .addLoreLine(obfuscated.copy().setStyle(Styles.LABEL)).build())
                .enabled(JSSTElementBuilder.from(Items.DRAGON_BREATH)
                        .addLoreLine(obfuscated.copy().setStyle(Styles.LABEL)).glow().build()).build(b -> {
                    Sounds.UI.click(player);
                    this.obfuscated = b;
                    this.refresh();
                }));

        this.setSlot(8, 1, JSSTElementBuilder.from(Items.KNOWLEDGE_BOOK).ui()
                .leftClick(Component.translatable("jsst.itemEditor.changeStyle.font"), () -> {
                    Sounds.UI.click(player);
                    this.openFontMenu();
                }));

        this.setSlot(8, 2, JSSTElementBuilder.from(Items.GRINDSTONE).ui().leftClick(Translations.clear(), () -> {
            Sounds.UI.grind(player);
            this.loadFromStyle(Style.EMPTY);
            this.refresh();
        }));

        this.setSlot(8, 3, CommonLabels.cancel(this::cancel));
    }

    private void loadFromStyle(Style style) {
        this.colour = style.getColor() != null ? Colour.fromInt(style.getColor().getValue()) : null;
        this.bold = style.isBold();
        this.italics = style.isItalic();
        this.underline = style.isUnderlined();
        this.strikethrough = style.isStrikethrough();
        this.obfuscated = style.isObfuscated();
        this.font = style.getFont();
    }

    private Component buildOutput() {
        Style style = Style.EMPTY.withBold(bold)
                .withItalic(italics)
                .withUnderlined(underline)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
                .withFont(font);

        String initial = this.initial.getString();

        if (this.colour == null || this.initial.getString().isBlank()) {
            return Component.literal(initial).withStyle(style);
        } else if (this.colour instanceof Colour col) {
            return Component.literal(initial).withStyle(style).withColor(col.toARGB());
        } else {
            return TextUtils.applyGradient(initial, style, this.colour);
        }
    }

    @Override
    protected void refresh() {
        UIRegion slots = UIRegion.rectangle(this, 0, 0, 4, 4);

        currentPage.draw(this.player, slots, gradient -> {
            Sounds.UI.click(player);
            this.colour = gradient;
            this.refresh();
        });

        this.setSlot(6, 3, JSSTElementBuilder.from(Items.PAPER).ui().setName(buildOutput())
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

    private Component createPreview(ResourceLocation font) {
        return Component.translatable("jsst.itemEditor.changeStyle.font.sample",
                    Component.translatable("advancements.adventure.who_needs_rockets.description").withStyle(Styles.CLEAN)
                .withStyle(Styles.CLEAN.withColor(ChatFormatting.YELLOW).withFont(font)));
    }

    private void openFontMenu() {
        Map<ResourceLocation, GuiElementInterface> fonts = new LinkedHashMap<>();

        fonts.put(DEFAULT_FONT, JSSTElementBuilder.from(Items.GRASS_BLOCK).ui()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.font.default"))
                .addLoreLine(createPreview(DEFAULT_FONT)).build());

        fonts.put(ALT_FONT, JSSTElementBuilder.from(Items.ENCHANTING_TABLE).ui()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.font.alt"))
                .addLoreLine(createPreview(ALT_FONT)).build());

        fonts.put(UNIFORM_FONT, JSSTElementBuilder.from(Items.COMPASS).ui()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.font.uniform"))
                .addLoreLine(createPreview(UNIFORM_FONT)).build());

        fonts.put(ILLAGER_FONT, JSSTElementBuilder.from(Raid.getOminousBannerInstance(RegistryUtils.lookup(this.player.serverLevel().registryAccess(), Registries.BANNER_PATTERN))).ui()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.font.illageralt"))
                .addLoreLine(createPreview(ILLAGER_FONT)).build());

        fonts.put(CUSTOM_FONT, JSSTElementBuilder.from(Items.NAME_TAG).ui()
                .setName(Component.translatable("jsst.itemEditor.changeStyle.font.custom"))
                .addLoreLine(Component.translatable("jsst.itemEditor.changeStyle.font.custom.disclaimer")
                        .withStyle(Styles.LABEL)).build());

        InputMenus.<ResourceLocation>selection(player)
                .title(Component.translatable("jsst.itemEditor.changeStyle.font.custom"))
                .options(fonts.keySet())
                .labels(fonts::get)
                .start(fontId -> {
            if (fontId.isPresent()) {
                if (CUSTOM_FONT.equals(fontId.get())) {
                    InputMenus.string(player).title(Component.translatable("jsst.itemEditor.changeStyle.font.custom"))
                            .initial(this.font == null ? Style.DEFAULT_FONT.toString() : this.font.toString())
                            .validator(s -> ResourceLocation.read(s).isSuccess()).start(opt -> {
                                opt.map(ResourceLocation::read).filter(DataResult::isSuccess)
                                        .ifPresent(resloc -> this.font = resloc.getOrThrow());
                                this.open();
                            });
                } else {
                    this.font = fontId.get();
                    this.open();
                }
            } else {
                this.open();
            }
        });
    }
}
