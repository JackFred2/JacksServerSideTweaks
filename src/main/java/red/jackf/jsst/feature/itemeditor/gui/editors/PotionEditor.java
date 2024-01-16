package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;
import red.jackf.jsst.util.sgui.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.SwitchButton;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PotionEditor extends GuiEditor {
    // MC-98310 either displayed duration is too short or applied duration is too long
    private static final Map<Item, Integer> POTION_DURATION_REDUCTION = Map.of(
            // Items.LINGERING_POTION, 4,
            // Items.TIPPED_ARROW, 8
    );

    private static final List<Item> VALID_ITEMS = List.of(
            Items.POTION,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.TIPPED_ARROW
    );

    public static final EditorType TYPE = new EditorType(
            JSST.id("potion"),
            PotionEditor::new,
            false,
            false,
            stack -> VALID_ITEMS.contains(stack.getItem()),
            PotionEditor::createLabel
    );
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private boolean mitigateItemSpecificDurationReduction = false;
    private final ListPaginator<MobEffectInstance> effectPaginator = ListPaginator.<MobEffectInstance>builder(this)
            .slots(4, 9, 2, 6)
            .list(this.effects)
            .max(20)
            .rowDraw(this::drawPageRow)
            .modifiable(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), false)
            .onUpdate(this::redraw).build();

    public PotionEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.potion"));
        this.loadFromStack();
        this.drawStatic();
    }

    private static AnimatedGuiElementBuilder createLabel(EditorContext context) {
        AnimatedGuiElementBuilderExt builder = new AnimatedGuiElementBuilderExt();

        for (Colour colour : List.of(Colours.RED, Colours.ORANGE, Colours.YELLOW, Colours.GREEN, Colours.LIGHT_BLUE, Colours.BLUE, Colours.PURPLE, Colours.MAGENTA)) {
            builder.addStack(JSSTElementBuilder.from(setColour(Items.POTION.getDefaultInstance(), colour))
                    .hideFlags()
                    .setName(Component.translatable("jsst.itemEditor.potion"))
                    .asStack());
        }

        builder.setInterval(20);

        return builder;
    }

    private static MobEffectInstance copy(
            MobEffectInstance original,
            @Nullable MobEffect effect,
            @Nullable Integer duration,
            @Nullable Integer amplifier) {
        MobEffect newEffect = effect != null ? effect : original.getEffect();
        int newDuration = duration != null ? duration : original.getDuration();
        int newAmplifier = amplifier != null ? amplifier : original.getAmplifier();
        return new MobEffectInstance(newEffect, newDuration, newAmplifier);
    }

    private static ItemStack setColour(ItemStack potionItem, @Nullable Colour colour) {
        if (colour == null) {
            potionItem.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_COLOR);
        } else {
            potionItem.getOrCreateTag().putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, colour.toARGB());
        }
        return potionItem;
    }

    public static Component describe(MobEffectInstance instance, float tickrate) {
        MutableComponent description = Component.translatable(instance.getDescriptionId());
        if (instance.getAmplifier() > 0) {
            description = Component.translatable(
                    "potion.withAmplifier",
                    description,
                    Component.translatable("potion.potency." + instance.getAmplifier())
            );
        }

        Component durationText;
        if (instance.getDuration() == -1) {
            durationText = Component.translatable("effect.duration.infinite");
        } else if (instance.getDuration() < 20) {
            durationText = Component.literal(instance.getDuration() + " ticks");
        } else {
            durationText = MobEffectUtil.formatDuration(instance, 1f, tickrate);
        }

        description = Component.translatable(
                "potion.withDuration",
                description,
                durationText
        );

        return description.withStyle(instance.getEffect().getCategory().getTooltipFormatting());
    }

    private void loadFromStack() {
        this.effects.clear();
        this.effects.addAll(PotionUtils.getCustomEffects(this.stack));
    }

    @Override
    protected void onReset() {
        this.loadFromStack();
    }

    protected void applyCustomEffectsToStack() {
        if (this.effects.isEmpty()) {
            this.stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_EFFECTS);
        } else if (this.mitigateItemSpecificDurationReduction) {
            var mitigated = new ArrayList<MobEffectInstance>(this.effects.size());
            for (MobEffectInstance effect : this.effects) {
                mitigated.add(copy(effect, null, effect.mapDuration(i -> i * POTION_DURATION_REDUCTION.getOrDefault(stack.getItem(), 1)), null));
            }
            PotionUtils.setCustomEffects(stack, mitigated);
        } else {
            PotionUtils.setCustomEffects(stack, this.effects);
        }
    }

    private void drawStatic() {
        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(this::cancel));

        for (int row = 0; row < 6; row++) this.setSlot(Util.slot(3, row), CommonLabels.divider());

        for (int col = 4; col < 9; col++) this.setSlot(Util.slot(col, 1), CommonLabels.divider());

        this.setSlot(Util.slot(0, 4), JSSTElementBuilder.ui(Items.RED_DYE)
                .leftClick(Component.translatable("jsst.itemEditor.colour.custom"), () -> {
                    Sounds.click(player);
                    EditorMenus.colour(player, result -> {
                        if (result.hasResult())
                            setColour(stack, result.result());
                        this.open();
                    });
                }));
    }

    // TODO move to lore editor
    private boolean isHidingAdditional() {
        return (((ItemStackAccessor) (Object) this.stack).jsst$itemEditor$getTooltipHideMask() & ItemStack.TooltipPart.ADDITIONAL.getMask()) != 0;
    }

    private void setHidingAdditional(boolean shouldHide) {
        if (shouldHide) {
            this.stack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        } else {
            Util.unhideTooltipPart(this.stack, ItemStack.TooltipPart.ADDITIONAL);
        }
    }

    private void changeBaseItem(Item item) {
        var newStack = item.getDefaultInstance();
        newStack.setTag(this.stack.getTag());
        this.stack = newStack;
        this.redraw();
    }

    @Override
    protected void redraw() {
        this.applyCustomEffectsToStack();
        this.drawPreview(Util.slot(1, 1));

        // change item type
        var itemTypeBuilder = SwitchButton.<Item>builder(Component.translatable("jsst.itemEditor.potion.setPotionItemType"));
        for (Item potionHoldingItem : VALID_ITEMS) {
            itemTypeBuilder.addOption(potionHoldingItem, JSSTElementBuilder.ui(potionHoldingItem)
                    .setName(potionHoldingItem.getDescription())
                    .hideFlags()
                    .dontCleanText()
                    .asStack());
        }
        this.setSlot(Util.slot(2, 3), itemTypeBuilder.setCallback(this::changeBaseItem)
                .build(this.stack.getItem()));

        // remove custom colour
        // noinspection DataFlowIssue
        if (this.stack.hasTag() && this.stack.getTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR, Tag.TAG_INT)) {
            this.setSlot(Util.slot(1, 4), JSSTElementBuilder.ui(Items.GUNPOWDER)
                    .leftClick(Component.translatable("jsst.itemEditor.colour.custom.remove"),
                            () -> {
                                Sounds.clear(player);
                                setColour(this.stack, null);
                                this.redraw();
                            }));
        } else {
            this.clearSlot(Util.slot(1, 4));
        }

        // hide potion tooltips
        this.setSlot(Util.slot(2, 4), ToggleButton.builder()
                .disabled(Items.ENDER_PEARL.getDefaultInstance())
                .enabled(Items.ENDER_EYE.getDefaultInstance())
                .label(Component.translatable("jsst.itemEditor.potion.hideAdditionalTooltipPart"))
                .initial(isHidingAdditional())
                .setCallback(newValue -> {
                    Sounds.click(player);
                    setHidingAdditional(newValue);
                    this.redraw();
                }).build());

        // duration mitigation if applicable
        if (POTION_DURATION_REDUCTION.getOrDefault(this.stack.getItem(), 1) != 1) {
            this.setSlot(Util.slot(0, 3), ToggleButton.builder()
                    .disabled(Items.CLOCK.getDefaultInstance())
                    .enabled(JSSTElementBuilder.from(Items.CLOCK).glow().asStack())
                    .label(Component.translatable("jsst.itemEditor.potion.compensateForItemReductions"))
                    .initial(this.mitigateItemSpecificDurationReduction)
                    .setCallback(newValue -> {
                        Sounds.click(player);
                        this.mitigateItemSpecificDurationReduction = newValue;
                        this.redraw();
                    })
                    .build());
        } else {
            this.clearSlot(Util.slot(0, 3));
        }

        // standard potion selection
        this.setSlot(Util.slot(4, 0), JSSTElementBuilder.from(LabelMaps.POTIONS.getLabel(PotionUtils.getPotion(this.stack)))
                .leftClick(Component.translatable("jsst.itemEditor.potion.setPotion"), () -> {
                    Sounds.click(player);
                    List<Potion> potions = this.context.server().registryAccess()
                            .registryOrThrow(Registries.POTION)
                            .stream().toList();
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.potion.setPotion"),
                            potions,
                            LabelMaps.POTIONS,
                            result -> {
                                if (result.hasResult())
                                    PotionUtils.setPotion(this.stack, result.result());
                                this.open();
                            });
                }));

        this.effectPaginator.draw();
    }

    private List<GuiElementInterface> drawPageRow(int index, MobEffectInstance instance) {
        var effect = JSSTElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(instance.getEffect()))
                .setName(describe(instance, this.context.server().tickRateManager().tickrate()))
                .leftClick(Component.translatable("jsst.itemEditor.potion.setEffect"), () -> {
                    Sounds.click(player);
                    SelectorMenu.open(player,
                            Component.translatable("jsst.itemEditor.potion.setEffect"),
                            this.context.server().registryAccess()
                                    .registryOrThrow(Registries.MOB_EFFECT).stream()
                                    .toList(),
                            LabelMaps.MOB_EFFECTS,
                            result -> {
                                if (result.hasResult())
                                    this.effects.set(index, copy(instance, result.result(), null, null));
                                this.open();
                            });
                }).build();

        var duration = JSSTElementBuilder.ui(Items.CLOCK)
                .leftClick(Component.translatable("jsst.itemEditor.potion.setDuration"), () -> {
                    Sounds.click(player);
                    Menus.duration(player,
                            Component.translatable("jsst.itemEditor.potion.setDuration"),
                            instance.isInfiniteDuration() ? "infinite" : instance.getDuration() + " ticks",
                            true,
                            result -> {
                                if (result.hasResult()) {
                                    int newDuration = result.result();
                                    this.effects.set(index, copy(instance, null, newDuration == Integer.MAX_VALUE ? -1 : result.result(), null));
                                }
                                this.open();
                            });
                }).build();

        var amplifier = JSSTElementBuilder.ui(Items.GLOWSTONE_DUST)
                .leftClick(Component.translatable("jsst.itemEditor.potion.setAmplifier"), () -> {
                    Sounds.click(player);
                    Menus.integer(player,
                            Component.translatable("jsst.itemEditor.potion.setAmplifier"),
                            instance.getAmplifier(),
                            0,
                            127,
                            null,
                            result -> {
                                if (result.hasResult())
                                    this.effects.set(index, copy(instance, null, null, result.result()));
                                this.open();
                            });
                }).build();

        return List.of(effect, duration, amplifier);
    }




}
