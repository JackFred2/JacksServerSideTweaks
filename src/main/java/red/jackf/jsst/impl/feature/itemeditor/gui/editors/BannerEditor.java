package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatternLayers.Layer;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.*;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.ListPaginator;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;
import red.jackf.jsst.impl.utils.sgui.region.UIRectangle;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BannerEditor extends GuiEditor {
    public static final Type<BannerEditor> TYPE = Editor.<BannerEditor>typeBuilder(JSST.id("banner"))
            .factory(BannerEditor::new)
            .labelFactory(BannerEditor::getLabel)
            .supportsCosmetic()
            .appliesTo(session -> session.getStack().getItem() instanceof BannerItem)
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        var banner = Banners.PMC.parsePMCCode(session.registries(), "bce2lfmdjek13")
                .map(Banners::create)
                .result()
                .orElse(Items.RED_BANNER.getDefaultInstance());

        return JSSTElementBuilder.from(banner).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.banner"))
                .hideDefaultTooltip()
                .build();
    }

    private DyeColor baseColour = DyeColor.WHITE;
    private final List<Layer> layers = new ArrayList<>();

    private final ListPaginator<Layer> paginator = ListPaginator.<Layer>builder(this)
            .slots(UIRegion.rectangle(this, 4, 0, 9, 6))
            .drawFunction(this::drawRow)
            .elements(layers)
            .modifiable(this::randomLayer, true, 16, this::refresh)
            .build();

    private Layer randomLayer() {
        var random = this.session.getPlayer().getRandom();

        var pattern = this.lookupRegistry(Registries.BANNER_PATTERN).getRandom(random).orElseThrow();
        var colour = ColourUtils.CANON_DYE_ORDER.get(random.nextIntBetweenInclusive(0, 15));

        return new Layer(pattern, colour);
    }

    private List<GuiElementInterface> drawRow(int index, Layer layer) {
        var pattern = JSSTElementBuilder.from(LabelMaps.BANNER_PATTERN
                .apply(layer.color())
                .apply(layer.pattern())).ui()
                .leftClick(Translations.change(), () -> {
                    Sounds.UI.click(player);

                    SelectionMenu.<Holder<BannerPattern>>builder(player)
                            .title(Component.translatable("jsst.itemEditor.editor.banner.setPattern"))
                            .labelStacks(LabelMaps.BANNER_PATTERN.apply(layer.color()))
                            .options(RegistryUtils.stream(this.lookupRegistry(Registries.BANNER_PATTERN)))
                            .start(opt -> {
                                opt.ifPresent(bannerPatternHolder -> this.layers.set(index, new Layer(bannerPatternHolder, layer.color())));

                                this.open();
                            });
                });

        var colour = JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(layer.color())).ui()
                .leftClick(Translations.change(), () -> {
                    Sounds.UI.click(player);

                    SelectionMenu.<DyeColor>builder(player)
                            .title(Component.translatable("jsst.itemEditor.setColour"))
                            .labelStacks(LabelMaps.DYE_COLOR)
                            .options(ColourUtils.CANON_DYE_ORDER)
                            .start(opt -> {
                                opt.ifPresent(newColour -> this.layers.set(index, new Layer(layer.pattern(), newColour)));

                                this.open();
                            });
                });

        return List.of(pattern.build(), colour.build());
    }

    public BannerEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.banner"), MenuType.GENERIC_9x6, false);

        this.loadPatterns();
        this.baseColour = this.stack.getItem() instanceof BannerItem bannerItem ? bannerItem.getColor() : DyeColor.WHITE;
    }

    @Override
    protected ItemStack buildOutput() {
        var stack = super.buildOutput();

        stack = stack.transmuteCopy(Banners.ByColour.ITEM.get(this.baseColour));

        if (this.layers.isEmpty()) {
            if (stack.getPrototype().has(DataComponents.BANNER_PATTERNS)) {
                stack.set(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
            } else {
                stack.remove(DataComponents.BANNER_PATTERNS);
            }
        } else {
            var builder = new BannerPatternLayers.Builder();
            for (Layer layer : layers) {
                builder.add(layer);
            }
            stack.set(DataComponents.BANNER_PATTERNS, builder.build());
        }
        return stack;
    }

    private void loadPatterns() {
        this.layers.clear();
        this.layers.addAll(this.stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers());
    }

    @Override
    protected void onReset() {
        this.baseColour = this.stack.getItem() instanceof BannerItem banner ? banner.getColor() : DyeColor.WHITE;
        this.loadPatterns();
    }

    @Override
    protected void drawStatic() {
        super.drawStatic();

        UIRegion.column(this, 3).fillElement(CommonElements::divider);

        this.setSlot(0, 4, JSSTElementBuilder.from(Heads.PMC).ui()
                .setName(Component.literal("Planet").withColor(0xFF_6EC310)
                        .append(Component.literal("Mine").withColor(0xFF_A3692B))
                        .append(Component.literal("Craft").withColor(0xFF_3DA2FF)))
                .addLoreLine(Component.translatable("jsst.itemEditor.editor.banner.notAffiliatedWithPMC").withStyle(Styles.LABEL))
                .leftClick(Translations.imprt(), () -> {
                    Sounds.UI.click(player);

                    InputMenus.string(player)
                            .validator(s -> Banners.PMC.parsePMCCode(this.player.registryAccess(), s).isSuccess())
                            .title(Component.translatable("jsst.itemEditor.editor.banner.importPMC"))
                            .hint(Component.translatable("jsst.itemEditor.editor.banner.importPMC.hint1"),
                                  Component.translatable("jsst.itemEditor.editor.banner.importPMC.hint2"))
                            .start(opt -> {
                                if (opt.isPresent()) {
                                    Pair<DyeColor, List<Layer>> parsed = Banners.PMC.parsePMCCode(this.player.registryAccess(), opt.get()).getOrThrow();

                                    this.baseColour = parsed.getFirst();
                                    this.layers.clear();
                                    this.layers.addAll(parsed.getSecond());
                                }

                                this.open();
                            });
                })
                .rightClick(Translations.export(), () -> {
                    Sounds.UI.click(player);

                    Banners.PMC.toPMCCode(Pair.of(this.baseColour, this.layers)).ifSuccess(code -> {
                        player.sendSystemMessage(Styles.clipboardCopy(code));
                    }).ifError(err -> {
                        player.sendSystemMessage(Component.translatable("jsst.itemEditor.editor.banner.exportPmcError", err.message()));
                    });
                }));

        this.setSlot(1, 3, AnimatedGuiElementBuilderExt.makeForEach(List.of("1dDfvfz1gfz", "1fDfvfz1gfz"), code -> {
            var stack = Banners.create(Banners.PMC.parsePMCCode(this.session.registries(), code).getOrThrow());

            return JSSTElementBuilder.from(stack).ui()
                    .setName(Component.translatable("jsst.itemEditor.editor.banner.swapColour"))
                    .hideDefaultTooltip()
                    .asStack();
            }).setInterval(20)
                .wrap()
                .leftClick(Translations.open(), () -> {
                    Sounds.UI.click(player);

                    new SwapColourMenu(this::open).open();
                }));
    }

    @Override
    protected void refresh() {
        this.drawPreview(1, 1);

        this.setSlot(0, 3, JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(baseColour)).ui()
                .modifyName(comp -> Component.translatable("jsst.itemEditor.editor.banner.base", comp))
                .leftClick(Translations.change(), () -> {
                    Sounds.UI.click(player);

                    SelectionMenu.<DyeColor>builder(player)
                            .title(Component.translatable("jsst.itemEditor.setColour"))
                            .options(ColourUtils.CANON_DYE_ORDER)
                            .labelStacks(LabelMaps.DYE_COLOR)
                            .start(opt -> {
                                opt.ifPresent(col -> this.baseColour = col);

                                this.open();
                            });
                }));

        this.setSlot(0, 5, CommonElements.cancel(this::cancel));

        this.paginator.draw();
    }

    private class SwapColourMenu extends SimpleGuiExt {
        private final Runnable callback;
        private @Nullable DyeColor from = null;
        private @Nullable DyeColor to = null;

        private final UIRectangle fromRegion = UIRegion.rectangle(this, 0, 0, 4, 4);
        private final UIRectangle toRegion = UIRegion.rectangle(this, 5, 0, 9, 4);

        private SwapColourMenu(Runnable callback) {
            super(MenuType.GENERIC_9x4, BannerEditor.this.player, false);
            this.callback = Callbacks.singleUse(callback);

            this.drawStatic();
        }

        @Override
        protected void drawStatic() {
            this.setSlot(4, 0, JSSTElementBuilder.from(Banners.create(Pair.of(BannerEditor.this.baseColour, BannerEditor.this.layers))).ui()
                    .hideTooltip());

            this.setSlot(4, 1, CommonElements.divider());

            this.setSlot(4, 3, CommonElements.cancel(() -> {
                Sounds.UI.close(player);

                this.callback.run();
            }));
        }

        @Override
        protected void refresh() {
            fromRegion.loadElements(ColourUtils.CANON_DYE_ORDER.stream()
                    .map(col -> BannerEditor.this.baseColour == col || BannerEditor.this.layers.stream().anyMatch(layer -> layer.color() == col) ? col : null)
                    .map(col -> {
                        if (col == null) return GuiElement.EMPTY;
                        return JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(col)).ui()
                                .modifyName(name -> Component.translatable("jsst.itemEditor.editor.banner.swapColour.from", name))
                                .addLoreLine(col == this.from, Translations.selected().withStyle(Styles.LABEL))
                                .glow(col == from)
                                .leftClick(Translations.select(), () -> {
                                    Sounds.UI.click(player);
                                    this.from = col;
                                    this.refresh();
                                })
                                .build();
                    }));

            if (this.from != null) {
                toRegion.loadElements(ColourUtils.CANON_DYE_ORDER.stream()
                        .map(col -> JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(col)).ui()
                                .modifyName(name -> Component.translatable("jsst.itemEditor.editor.banner.swapColour.to", name))
                                .addLoreLine(col == this.to, Translations.selected().withStyle(Styles.LABEL))
                                .glow(col == to)
                                .leftClick(Translations.select(), () -> {
                                    Sounds.UI.click(player);
                                    this.to = col;
                                    this.refresh();
                                })
                                .build()));
            } else {
                toRegion.fillElement(CommonElements::disabled);
            }

            DyeColor base;
            List<Layer> layers;

            if (this.from != null && this.to != null) {
                base = this.from == BannerEditor.this.baseColour ? this.to : BannerEditor.this.baseColour;
                layers = BannerEditor.this.layers.stream()
                        .map(layer -> new Layer(layer.pattern(), layer.color() == this.from ? this.to : layer.color()))
                        .toList();
            } else {
                layers = BannerEditor.this.layers;
                base = BannerEditor.this.baseColour;
            }

            this.setSlot(4, 2, JSSTElementBuilder.flatCopy(Banners.create(Pair.of(base, layers))).ui()
                    .leftClick(Translations.confirm(), () -> {
                        Sounds.UI.click(player);

                        if (this.from != null && this.to != null) {
                            BannerEditor.this.baseColour = base;
                            BannerEditor.this.layers.clear();
                            BannerEditor.this.layers.addAll(layers);
                        }

                        this.callback.run();
                    }));
        }
    }
}
