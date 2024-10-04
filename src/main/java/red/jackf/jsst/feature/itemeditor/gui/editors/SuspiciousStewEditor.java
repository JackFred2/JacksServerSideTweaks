package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.mixins.itemeditor.SuspiciousStewItemAccessor;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SuspiciousStewEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            JSST.id("suspicious_stew"),
            SuspiciousStewEditor::new,
            false,
            false,
            stack -> stack.is(Items.SUSPICIOUS_STEW),
            context -> JSSTElementBuilder.from(Items.SUSPICIOUS_STEW).setName(Component.translatable("jsst.itemEditor.suspiciousStew"))
    );

    private final List<SuspiciousEffectHolder.EffectEntry> effects = new ArrayList<>();

    public SuspiciousStewEditor(ServerPlayer player,
                                EditorContext context,
                                ItemStack initial,
                                Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.drawStatic();

        this.loadFromStack();
    }

    private final ListPaginator<SuspiciousEffectHolder.EffectEntry> paginator = ListPaginator.<SuspiciousEffectHolder.EffectEntry>builder(this)
            .slots(4, 9, 0, 6)
            .list(this.effects)
            .modifiable(() -> new SuspiciousEffectHolder.EffectEntry(MobEffects.MOVEMENT_SPEED, 160), false)
            .max(20)
            .onUpdate(this::redraw)
            .rowDraw(this::drawRow)
            .build();

    private void loadFromStack() {
        this.effects.clear();
        SuspiciousStewItemAccessor.jsst$itemEditor$listPotionEffects(this.stack, this.effects::add);
    }

    @Override
    protected void onReset() {
        this.loadFromStack();
    }

    private List<GuiElementInterface> drawRow(int index, SuspiciousEffectHolder.EffectEntry entry) {
        GuiElement effect = JSSTElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(entry.effect()))
                .setName(PotionEditor.describe(entry.createEffectInstance(), this.context.server().tickRateManager().tickrate()))
                .leftClick(Component.translatable("jsst.itemEditor.potion.setEffect"), () -> {
                    Sounds.click(player);
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.potion.setEffect"),
                            this.context.server().registryAccess().registryOrThrow(Registries.MOB_EFFECT).stream()
                                    .toList(),
                            LabelMaps.MOB_EFFECTS,
                            result -> {
                                if (result.hasResult())
                                    this.effects.set(index, new SuspiciousEffectHolder.EffectEntry(result.result(), entry.duration()));
                                this.open();
                            });
                }).build();

        GuiElement duration = JSSTElementBuilder.ui(Items.CLOCK)
                .leftClick(Component.translatable("jsst.itemEditor.potion.setDuration"), () -> {
                    Sounds.click(player);
                    Menus.duration(player,
                            Component.translatable("jsst.itemEditor.potion.setDuration"),
                            entry.duration() == -1 ? "infinite" : entry.duration() + " ticks",
                            true,
                            result -> {
                                if (result.hasResult()) {
                                    int newDuration = result.result();
                                    this.effects.set(index, new SuspiciousEffectHolder.EffectEntry(entry.effect(), newDuration == Integer.MAX_VALUE ? -1 : result.result()));
                                }
                                this.open();
                            });
                }).build();

        return List.of(effect, duration);
    }

    private void drawStatic() {
        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(this::cancel));

        this.setSlot(Util.slot(0, 4), JSSTElementBuilder.ui(Items.OXEYE_DAISY)
                        .leftClick(Component.translatable("jsst.itemEditor.suspiciousStew.choosePreset"), () -> {
                    Sounds.click(player);
                    List<Item> options = SuspiciousEffectHolder.getAllEffectHolders().stream()
                            .map(holder -> {
                                if (holder instanceof Block block) {
                                    Item asItem = block.asItem();
                                    if (asItem != Items.AIR) return asItem;
                                } else if (holder instanceof Item item) {
                                    return item;
                                }
                                return null;
                            }).filter(Objects::nonNull)
                            .toList();

                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.suspiciousStew.choosePreset"),
                            options,
                            LabelMaps.ITEMS_WITH_SUSPICIOUS_STEW_EFFECTS,
                            result -> {
                                if (result.hasResult()) {
                                    var holder = SuspiciousEffectHolder.tryGet(result.result());
                                    if (holder != null) {
                                        this.effects.clear();
                                        this.effects.addAll(holder.getSuspiciousEffects().effects());
                                    }
                                }
                                this.open();
                            });
                }));

        for (int row = 0; row < 6; row++)
            this.setSlot(Util.slot(3, row), CommonLabels.divider());
    }

    @Override
    protected void redraw() {
        this.drawPreview(Util.slot(1, 1));

        SuspiciousStewItem.saveMobEffects(this.stack, this.effects);

        this.paginator.draw();
    }


}
