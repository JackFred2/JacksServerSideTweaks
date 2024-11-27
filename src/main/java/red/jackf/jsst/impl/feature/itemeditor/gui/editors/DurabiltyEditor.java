package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import com.google.common.collect.Streams;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.CycleButton;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.menus.InputMenus;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DurabiltyEditor extends GuiEditor {
    public static final Type<DurabiltyEditor> TYPE = Editor.<DurabiltyEditor>typeBuilder(JSST.id("durability"))
            .labelFactory(DurabiltyEditor::getLabel).factory(DurabiltyEditor::new)
            .appliesTo(session -> session.getStack().has(DataComponents.MAX_DAMAGE)).build();
    private static final List<Integer> DURABILITY_VALUE_BREAKS = List.of(1, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000);
    private static final List<Float> DURABILITY_PERCENTAGE_BREAKS = List.of(1f, 2.5f, 5f, 10f, 25f, 100f / 3, 50f, 75f, 95f, 100f);
    private final UIRegion valueSlots = UIRegion.rectangle(this, 4, 0, 9, 3);
    private final UIRegion percentSlots = UIRegion.rectangle(this, 4, 4, 9, 6);
    private Page currentlyEditing = Page.DURABILITY;

    public DurabiltyEditor(EditSession session, Consumer<Result> resultConsumer) {
        super(session, resultConsumer, Component.translatable("jsst.itemEditor.editor.durability"), MenuType.GENERIC_9x6, false);

        this.refreshTitle();
    }

    private static GuiElementInterface getLabel(EditSession session) {
        return JSSTElementBuilder.from(Items.ANVIL).ui()
                .setName(Component.translatable("jsst.itemEditor.editor.durability")).build();
    }

    private void refreshTitle() {
        this.setTitle(Translations.split(Component.translatable("jsst.itemEditor.editor.durability"), Component.translatable("jsst.itemEditor.editor.currentMax", this.stack.getMaxDamage())));
    }

    @Override
    protected void drawStatic() {
        UIRegion.column(this, 3).fillElement(CommonElements::divider);
        UIRegion.row(this, 3, 4, 9).fillElement(CommonElements::divider);

        this.setSlot(0, 4, CycleButton.<Page>builder(Component.translatable("jsst.itemEditor.editor.durability.currentlyEditing"))
                .option(Page.DURABILITY, JSSTElementBuilder.from(Items.IRON_INGOT).ui()
                        .setName(Component.translatable("jsst.itemEditor.editor.durability.currentlyEditing.durability"))
                        .build())
                .option(Page.MAX_DURABILITY, JSSTElementBuilder.from(Items.IRON_INGOT).ui().glow()
                        .setName(Component.translatable("jsst.itemEditor.editor.durability.currentlyEditing.maxDurability"))
                        .build())
                .build(page -> {
                    Sounds.UI.click(player);
                    this.currentlyEditing = page;
                    this.refresh();
                }));

        //noinspection DataFlowIssue
        UnbreakableState currentUnbreakableState = this.stack.has(DataComponents.UNBREAKABLE) ? (this.stack.get(DataComponents.UNBREAKABLE).showInTooltip() ? UnbreakableState.YES_SHOWN : UnbreakableState.YES_HIDDEN) : UnbreakableState.NO;

        this.setSlot(1, 4, CycleButton.<UnbreakableState>builder(Component.translatable("jsst.itemEditor.editor.durability.unbreakable"))
                .initial(currentUnbreakableState)
                .option(UnbreakableState.NO, JSSTElementBuilder.from(Items.GLASS).ui()
                        .setName(Component.translatable("jsst.itemEditor.editor.durability.unbreakable.no"))
                        .build())
                .option(UnbreakableState.YES_SHOWN, JSSTElementBuilder.from(Items.NETHERITE_BLOCK).ui().glow()
                        .setName(Component.translatable("jsst.itemEditor.editor.durability.unbreakable.yesShown"))
                        .build())
                .option(UnbreakableState.YES_HIDDEN, JSSTElementBuilder.from(Items.NETHERITE_BLOCK).ui()
                        .setName(Component.translatable("jsst.itemEditor.editor.durability.unbreakable.yesHidden"))
                        .build())
                .build(unbreakable -> {
                    Sounds.UI.click(player);
                    switch (unbreakable) {
                        case NO -> this.stack.remove(DataComponents.UNBREAKABLE);
                        case YES_SHOWN -> this.stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
                        case YES_HIDDEN -> this.stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                    }
                    this.refresh();
                }));

        this.setSlot(0, 5, CommonElements.cancel(this::cancel));
    }

    @Override
    protected void onReset() {
        this.refreshTitle();
    }

    @Override
    protected void refresh() {
        this.drawPreview(1, 1);

        this.drawPage();
    }

    private void drawPage() {
        final int max = this.stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        if (max > 0 && !this.stack.has(DataComponents.UNBREAKABLE)) {
            if (currentlyEditing == Page.DURABILITY) {
                this.valueSlots.loadElements(Streams.concat(DURABILITY_VALUE_BREAKS.stream().filter(value -> value <= max)
                        .map(durability -> {
                            int damage = max - durability;
                            return JSSTElementBuilder.flatCopy(this.stack)
                                    .setName(Component.literal(String.valueOf(durability)))
                                    .setDamage(damage)
                                    .leftClick(Translations.select(), () -> {
                                        Sounds.UI.click(player);

                                        this.stack.setDamageValue(damage);
                                        this.refresh();
                                    }).build();
                        }), Stream.of(JSSTElementBuilder.from(Items.NAME_TAG).ui()
                            .leftClick(Component.translatable("jsst.itemEditor.editor.durability.setCustom"), () -> {
                                Sounds.UI.click(player);

                                InputMenus.integer(player, 1, max)
                                        .initial(String.valueOf(max - this.stack.getDamageValue()))
                                        .title(Component.translatable("jsst.itemEditor.editor.durability.currentlyEditing.durability"))
                                        .start(opt -> {
                                            opt.ifPresent(durability -> this.stack.setDamageValue(max - durability));

                                            this.open();
                                        });
                            }).build())
                ));
                this.percentSlots.loadElements(DURABILITY_PERCENTAGE_BREAKS.stream().map(durabilityPercent -> {
                    int durability = (int) (max * (durabilityPercent / 100f));
                    int damage = max - durability;
                    return JSSTElementBuilder.flatCopy(this.stack)
                            .setName(Component.literal("%.1f%%".formatted(durabilityPercent)))
                            .addLoreLine(Component.literal(String.valueOf(durability)).withStyle(Styles.MINOR_LABEL))
                            .setDamage(damage)
                            .leftClick(Translations.select(), () -> {
                                Sounds.UI.click(player);

                                this.stack.setDamageValue(damage);
                                this.refresh();
                            }).build();
                }));
            } else {
                final int currentDurability = max - this.stack.getDamageValue();

                this.valueSlots.loadElements(Streams.concat(DURABILITY_VALUE_BREAKS.stream().map(maxDurability -> {
                    final int adjustedDamage = maxDurability - currentDurability;

                    return JSSTElementBuilder.flatCopy(this.stack)
                            .setName(Component.literal(String.valueOf(maxDurability)))
                            .setMaxDamage(maxDurability)
                            .setDamage(adjustedDamage)
                            .glow()
                            .leftClick(Translations.select(), () -> {
                                Sounds.UI.click(player);

                                this.stack.set(DataComponents.MAX_DAMAGE, maxDurability);
                                this.stack.setDamageValue(adjustedDamage);
                                this.refreshTitle();
                                this.refresh();
                            }).build();
                }), Stream.of(JSSTElementBuilder.from(Items.NAME_TAG).ui()
                        .leftClick(Component.translatable("jsst.itemEditor.editor.durability.setCustom"), () -> {
                            Sounds.UI.click(player);

                            InputMenus.integer(player, 1, null)
                                    .initial(String.valueOf(max))
                                    .title(Component.translatable("jsst.itemEditor.editor.durability.currentlyEditing.maxDurability"))
                                    .start(opt -> {
                                        opt.ifPresent(newMax -> {
                                            final int newDamage = newMax - currentDurability;

                                            this.stack.set(DataComponents.MAX_DAMAGE, newMax);
                                            this.stack.setDamageValue(newDamage);
                                            this.refreshTitle();
                                        });

                                        this.open();
                                    });
                        }).build())
                ));

                this.percentSlots.fillElement(CommonElements::disabled);
            }
        } else {
            this.valueSlots.fillElement(CommonElements::disabled);
            this.percentSlots.fillElement(CommonElements::disabled);
        }
    }

    private enum Page {
        DURABILITY, MAX_DURABILITY
    }

    private enum UnbreakableState {
        NO,
        YES_SHOWN,
        YES_HIDDEN
    }
}
