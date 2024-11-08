package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.PotDecorations;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Cycling;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.elements.pagination.GridPaginator;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DecoratedPotEditor extends GuiEditor {
    public static final Type<DecoratedPotEditor> TYPE = Editor.<DecoratedPotEditor>typeBuilder(JSST.id("decorated_pot"))
            .factory(DecoratedPotEditor::new)
            .labelFactory(DecoratedPotEditor::getLabel)
            .appliesTo(session -> session.getStack().is(Items.DECORATED_POT))
            .build();

    private static GuiElementInterface getLabel(EditSession session) {
        AnimatedGuiElementBuilderExt builder = new AnimatedGuiElementBuilderExt()
                .setRandom(true)
                .setInterval(10);

        Registry<Item> reg = RegistryUtils.lookup(session.registries(), Registries.ITEM);

        reg.get(ItemTags.DECORATED_POT_INGREDIENTS).map(set -> set.stream().map(Holder::value))
                .orElseGet(() -> Stream.of(Items.DECORATED_POT))
                .forEach(item -> builder.addStack(JSSTElementBuilder.from(item).ui()
                        .setName(Component.translatable("jsst.itemEditor.editor.decoratedPot"))
                        .hideDefaultTooltip()
                        .asStack()));

        return builder.build();
    }

    private static final Item BLANK = Items.BRICK;

    private final GridPaginator<Item> paginator = GridPaginator.<Item>builder(this)
            .slots(UIRegion.rectangle(this, 6, 0, 9, 3))
            .elements(this.lookupRegistry(Registries.ITEM).get(ItemTags.DECORATED_POT_INGREDIENTS).orElseThrow().stream().map(Holder::value).toList())
            .fullButtons(this.getSlotFor(6, 3), this.getSlotFor(7, 3), this.getSlotFor(8, 3))
            .drawFunction((elementIndex, sherd) -> JSSTElementBuilder.from(sherd).ui()
                    .setName(sherd.getName())
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(player);
                        this.setFace(this.currentFace, sherd);
                        this.refresh();
                    })
                    .hideDefaultTooltip()
                    .build())
            .build();

    private Face currentFace = Face.FRONT;

    public DecoratedPotEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.decoratedPot"), MenuType.GENERIC_9x6, false);
    }

    private void update(UnaryOperator<PotDecorations> operation) {
        this.stack.update(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY, operation);
    }

    @Override
    protected void drawStatic() {
        UIRegion.rectangle(this, 0, 0, 5, 5).fillElement(CommonElements::disabled);
        UIRegion.column(this, 5).fillElement(CommonElements::divider);
        UIRegion.row(this, 4, 6, 9).fillElement(CommonElements::divider);
        UIRegion.row(this, 5, 1, 5).fillElement(CommonElements::disabled);

        this.setSlot(0, 5, JSSTElementBuilder.from(Items.COAL).ui()
                .leftClick(Component.translatable("jsst.itemEditor.editor.decoratedPot.rotateLeft"), () -> {
                    Sounds.UI.click(player);
                    this.update(old -> new PotDecorations(
                                    old.left(),
                                    old.front(),
                                    old.back(),
                                    old.right()
                            ));
                    this.currentFace = Cycling.previous(Face.ROTATION_ORDER, this.currentFace);
                    this.refresh();
                }));

        this.setSlot(2, 5, JSSTElementBuilder.from(Items.GOLD_INGOT).ui()
                .leftClick(Component.translatable("jsst.itemEditor.editor.decoratedPot.flip"), () -> {
                    Sounds.UI.click(player);
                    this.update(old -> new PotDecorations(
                                    old.front(),
                                    old.left(),
                                    old.right(),
                                    old.back()
                            ));
                    if (this.currentFace == Face.FRONT) {
                        this.currentFace = Face.BACK;
                    } else if (this.currentFace == Face.BACK) {
                        this.currentFace = Face.FRONT;
                    }
                    this.refresh();
                }));

        this.setSlot(4, 5, JSSTElementBuilder.from(Items.DIAMOND).ui()
                .leftClick(Component.translatable("jsst.itemEditor.editor.decoratedPot.rotateRight"), () -> {
                    Sounds.UI.click(player);
                    this.update(old -> new PotDecorations(
                                    old.right(),
                                    old.back(),
                                    old.front(),
                                    old.left()
                            ));
                    this.currentFace = Cycling.next(Face.ROTATION_ORDER, this.currentFace);
                    this.refresh();
                }));

        this.setSlot(6, 5, JSSTElementBuilder.from(Items.GRINDSTONE).ui()
                .leftClick(Translations.clear(), () -> {
                    Sounds.UI.grind(player);
                    for (Face face : Face.values()) {
                        this.setFace(face, BLANK);
                    }
                    this.refresh();
                }));

        this.setSlot(7, 5, JSSTElementBuilder.from(Items.NETHER_STAR).ui()
                .leftClick(Translations.randomize(), () -> {
                    Sounds.UI.click(player);
                    for (Face face : Face.values()) {
                        this.setFace(face,
                                this.lookupRegistry(Registries.ITEM).getRandomElementOf(ItemTags.DECORATED_POT_INGREDIENTS, this.player.getRandom())
                                    .map(Holder::value)
                                    .orElse(BLANK));
                    }
                    this.refresh();
                }));

        this.setSlot(8, 5, CommonElements.cancel(this::cancel));
    }

    @Override
    protected void refresh() {
        this.drawPreview(2, 2);

        for (Face face : Face.values()) {
            face.draw(this);
        }

        this.paginator.draw();
    }

    private void setFace(Face face, Item ingredient) {
        Optional<Item> newIngredient = ingredient == BLANK ? Optional.empty() : Optional.of(ingredient);

        switch (face) {
            case BACK -> update(old -> new PotDecorations(newIngredient, old.left(), old.right(), old.front()));
            case LEFT -> update(old -> new PotDecorations(old.back(), newIngredient, old.right(), old.front()));
            case RIGHT -> update(old -> new PotDecorations(old.back(), old.left(), newIngredient, old.front()));
            case FRONT -> update(old -> new PotDecorations(old.back(), old.left(), old.right(), newIngredient));
        }
    }

    private enum Face {
        BACK(10, List.of(0, 1, 9), () -> Component.translatable("jsst.itemEditor.editor.decoratedPot.back"), PotDecorations::back),
        LEFT(28, List.of(27, 36, 37), () -> Component.translatable("jsst.itemEditor.editor.decoratedPot.left"), PotDecorations::left),
        RIGHT(12, List.of(3, 4, 13), () -> Component.translatable("jsst.itemEditor.editor.decoratedPot.right"), PotDecorations::right),
        FRONT(30, List.of(31,  39, 40), () -> Component.translatable("jsst.itemEditor.editor.decoratedPot.front"), PotDecorations::front);

        private static final List<Face> ROTATION_ORDER = List.of(BACK, LEFT, FRONT, RIGHT);

        private final int ingredientSlot;
        private final List<Integer> highlightSlots;
        private final Supplier<MutableComponent> label;
        private final Function<PotDecorations, Optional<Item>> ingredientGet;

        Face(int ingredientSlot, List<Integer> highlightSlots, Supplier<MutableComponent> label, Function<PotDecorations, Optional<Item>> ingredientGet) {
            this.ingredientSlot = ingredientSlot;
            this.highlightSlots = highlightSlots;
            this.label = label;
            this.ingredientGet = ingredientGet;
        }

        void draw(DecoratedPotEditor editor) {
            Item toDraw = this.ingredientGet.apply(editor.stack.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY)).orElse(BLANK);

            editor.setSlot(this.ingredientSlot, JSSTElementBuilder.from(toDraw).ui()
                    .setName(toDraw.getName())
                    .addLoreLine(label.get().withStyle(Styles.MINOR_LABEL))
                    .leftClick(Translations.select(), () -> {
                        Sounds.UI.click(editor.player);
                        editor.currentFace = this;
                        editor.refresh();
                    })
                    .rightClick(Translations.clear(), () -> {
                        Sounds.UI.grind(editor.player);
                        editor.setFace(this, BLANK);
                        editor.refresh();
                    }));

            for (Integer highlightSlot : this.highlightSlots) {
                editor.setSlot(highlightSlot, editor.currentFace == this ? CommonElements.highlight() : CommonElements.disabled());
            }
        }
    }
}
