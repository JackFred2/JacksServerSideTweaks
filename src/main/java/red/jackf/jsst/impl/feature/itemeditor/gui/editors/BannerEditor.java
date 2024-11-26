package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatternLayers.Layer;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.ColourUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.ListPaginator;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BannerEditor extends GuiEditor {
    public static final Type<BannerEditor> TYPE = Editor.<BannerEditor>typeBuilder(JSST.id("banner"))
            .factory(BannerEditor::new)
            .labelFactory(BannerEditor::getLabel)
            .supportsCosmetic()
            .appliesTo(session -> session.getStack().is(ItemTags.BANNERS))
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.RED_BANNER.getDefaultInstance()).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.banner"))
                .hideDefaultTooltip()
                .build();
    }

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
                            .labelStacks(LabelMaps.BANNER_PATTERN.apply(layer.color()))
                            .options(this.lookupRegistry(Registries.BANNER_PATTERN).listElements().map(ref -> ref))
                            .start(opt -> {
                                opt.ifPresent(bannerPatternHolder -> this.layers.set(index, new Layer(bannerPatternHolder, layer.color())));

                                this.open();
                            });
                });

        var colour = JSSTElementBuilder.from(LabelMaps.DYE_COLOR.apply(layer.color())).ui()
                .leftClick(Translations.change(), () -> {
                    Sounds.UI.click(player);

                    SelectionMenu.<DyeColor>builder(player)
                            .labelStacks(LabelMaps.DYE_COLOR)
                            .options(ColourUtils.CANON_DYE_ORDER)
                            .start(opt -> {
                                opt.ifPresent(newColour -> this.layers.set(index, new Layer(layer.pattern(), newColour)));

                                this.open();
                            });
                });

        return List.of(pattern.build(), colour.build());
    }

    @Override
    protected ItemStack buildOutput() {
        var stack = super.buildOutput();
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

    public BannerEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.banner"), MenuType.GENERIC_9x6, false);

        this.loadPatterns();
    }

    private void loadPatterns() {
        this.layers.clear();

        this.layers.addAll(stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers());
    }

    @Override
    protected void reset() {
        super.reset();

        this.loadPatterns();
    }

    @Override
    protected void drawStatic() {
        super.drawStatic();

        UIRegion.column(this, 3).fillElement(CommonElements::divider);
    }

    @Override
    protected void refresh() {
        this.drawPreview(1, 1);

        this.setSlot(0, 5, CommonElements.cancel(this::cancel));

        this.paginator.draw();
    }
}
