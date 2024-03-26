package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.elements.SwitchButton;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.menus.selector.SelectorMenu;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.*;
import java.util.function.Consumer;

public class AttributeEditor extends GuiEditor {
    private static final int MAX = 20;
    private static final String KEY = "AttributeModifiers";
    private static final String KEY_ATTRIBUTE = "AttributeName";
    private static final String KEY_SLOT = "Slot";
    private static final String KEY_OPERATION = "Operation";
    private static final String KEY_AMOUNT = "Amount";
    private static final Map<@Nullable EquipmentSlot, AttributeSlot> BY_EQUIPMENT = new HashMap<>();
    public static final EditorType TYPE = new EditorType(
            JSST.id("attribute"),
            AttributeEditor::new,
            false,
            false,
            ignored -> true,
            ctx -> JSSTElementBuilder.ui(Items.ARMOR_STAND)
                    .setName(Component.translatable("jsst.itemEditor.attribute"))
    );
    private final List<AttributeModInstance> attributes = new ArrayList<>();    private final ListPaginator<AttributeModInstance> paginator = ListPaginator.<AttributeModInstance>builder(this)
            .list(this.attributes)
            .slots(4, 9, 0, 6)
            .onUpdate(this::redraw)
            .modifiable(this::generateNew, false)
            .rowDraw(this::drawRow)
            .max(MAX)
            .build();

    public AttributeEditor(ServerPlayer player,
                           EditorContext context,
                           ItemStack initial,
                           Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.loadFromStack();

        this.drawStatic();
    }

    private List<GuiElementInterface> drawRow(int index, AttributeModInstance instance) {
        boolean positive = instance.amount() >= 0.0;
        String text = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(
                instance.operation() == AttributeModifier.Operation.ADDITION ? instance.amount() : instance.amount() * 100
        );
        Component title = Component.translatable(
                "attribute.modifier." + (positive ? "plus" : "take") + "." + instance.operation().toValue(),
                text,
                Component.translatable(instance.attribute().getDescriptionId())
        ).withStyle(positive ? ChatFormatting.BLUE : ChatFormatting.RED);
        JSSTElementBuilder attributeElementBuilder = JSSTElementBuilder.ui(Items.BOOK)
                .setName(title);
        if (instance.slot() != AttributeSlot.ANY)
            attributeElementBuilder.addLoreLine(Component.translatable(
                    "jsst.itemEditor.attribute.slot.prefix",
                    instance.slot().name
            ).withStyle(Styles.MINOR_LABEL));
        attributeElementBuilder.leftClick(Translations.change(), () -> {
            Sounds.click(player);
            SelectorMenu.<Attribute>builder(player)
                    .title(Component.translatable("jsst.itemEditor.attribute.change"))
                    .options(this.context.server().registryAccess().registryOrThrow(Registries.ATTRIBUTE).stream())
                    .labelMap(LabelMaps.ATTRIBUTES)
                    .createAndShow(result -> {
                        if (result.hasResult()) {
                            this.attributes.set(
                                    index,
                                    new AttributeModInstance(instance.slot(), result.result(), instance.operation(), instance.amount())
                            );
                        }
                        this.open();
                    });
        });
        GuiElementInterface attributeElement = attributeElementBuilder.build();

        String amountText = instance.operation() == AttributeModifier.Operation.ADDITION ? text : text + "%";
        GuiElementInterface amountElement = JSSTElementBuilder.ui(Items.CLOCK)
                .setName(Component.translatable(
                        "jsst.itemEditor.attribute.amount",
                        Component.literal(amountText).withStyle(positive ? ChatFormatting.BLUE : ChatFormatting.RED)
                )).leftClick(Translations.change(), () -> {
                    Sounds.click(player);
                    @Nullable Double min = null;
                    @Nullable Double max = null;
                    if (instance.attribute() instanceof RangedAttribute ranged) {
                        min = ranged.getMinValue();
                        max = ranged.getMaxValue();
                    }
                    Menus.ddouble(player,
                            Component.translatable("jsst.itemEditor.attribute.amount.change"),
                            instance.amount(),
                            min,
                            max,
                            null,
                            result -> {
                                if (result.hasResult()) {
                                    this.attributes.set(
                                            index,
                                            new AttributeModInstance(instance.slot(), instance.attribute(), instance.operation(), instance.attribute().sanitizeValue(result.result()))
                                    );
                                }

                                this.open();
                            });
                }).build();

        GuiElementInterface operationElement = SwitchButton.<AttributeModifier.Operation>builder(Component.translatable("jsst.itemEditor.attribute.operation"))
                .addOption(AttributeModifier.Operation.ADDITION, JSSTElementBuilder.ui(Items.APPLE)
                        .setName(Component.translatable("jsst.itemEditor.attribute.operation.0"))
                        .dontCleanText()
                        .asStack())
                .addOption(AttributeModifier.Operation.MULTIPLY_BASE, JSSTElementBuilder.ui(Items.GOLDEN_APPLE)
                        .setName(Component.translatable("jsst.itemEditor.attribute.operation.1"))
                        .dontCleanText()
                        .asStack())
                .addOption(AttributeModifier.Operation.MULTIPLY_TOTAL, JSSTElementBuilder.ui(Items.ENCHANTED_GOLDEN_APPLE)
                        .setName(Component.translatable("jsst.itemEditor.attribute.operation.2"))
                        .dontCleanText()
                        .asStack())
                .setCallback(newOperation -> {
                    this.attributes.set(
                            index,
                            new AttributeModInstance(instance.slot(), instance.attribute(), newOperation, instance.amount())
                    );
                    this.redraw();
                }).build(instance.operation());

        GuiElementInterface slotElement = SwitchButton.<AttributeSlot>builder(Component.translatable("jsst.itemEditor.attribute.slot"))
                .addOptions(AttributeSlot.values(), slot -> slot.label)
                .setCallback(newSlot -> {
                    this.attributes.set(
                            index,
                            new AttributeModInstance(newSlot, instance.attribute(), instance.operation(), instance.amount())
                    );
                    this.redraw();
                }).build(instance.slot());

        return List.of(
                attributeElement,
                amountElement,
                operationElement,
                slotElement
        );
    }

    private AttributeModInstance generateNew() {
        return new AttributeModInstance(
                AttributeSlot.ANY,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADDITION,
                0.25
        );
    }

    private void loadFromStack() {
        this.attributes.clear();
        if (this.stack.hasTag()) {
            //noinspection DataFlowIssue
            ListTag attributes = this.stack.getTag().getList(KEY, CompoundTag.TAG_COMPOUND);
            for (Tag _attribute : attributes) {
                try {
                    CompoundTag tag = (CompoundTag) _attribute;
                    Attribute attribute = this.context.server().registryAccess()
                            .registryOrThrow(Registries.ATTRIBUTE)
                            .get(ResourceLocation.tryParse(tag.getString(KEY_ATTRIBUTE)));
                    if (attribute == null) continue;
                    AttributeSlot slot = AttributeSlot.ANY;
                    if (tag.contains(KEY_SLOT, Tag.TAG_STRING))
                        slot = BY_EQUIPMENT.getOrDefault(EquipmentSlot.byName(tag.getString(KEY_SLOT)), AttributeSlot.ANY);
                    AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(tag.getInt(KEY_OPERATION));
                    double amount = attribute.sanitizeValue(tag.getDouble(KEY_AMOUNT));
                    this.attributes.add(new AttributeModInstance(slot, attribute, operation, amount));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void updateStack() {
        this.stack.removeTagKey(KEY);

        for (AttributeModInstance attribute : this.attributes) {
            this.stack.addAttributeModifier(
                    attribute.attribute(),
                    new AttributeModifier("User-supplied Custom Attribute", attribute.amount(), attribute.operation()),
                    attribute.slot().slot
            );
        }
    }

    @Override
    protected void onReset() {
        this.loadFromStack();
    }

    private void drawStatic() {
        Util.fill(this, CommonLabels.divider(), 3, 4, 0, 6);
        this.setSlot(Util.slot(2, 5), CommonLabels.cancel(this::cancel));
    }

    @Override
    protected void redraw() {
        this.updateStack();
        this.drawPreview(Util.slot(1, 1));
        this.paginator.draw();

        boolean showingTooltip = (((ItemStackAccessor) (Object) this.stack).jsst$itemEditor$getTooltipHideMask() & ItemStack.TooltipPart.MODIFIERS.getMask()) == 0;
        this.setSlot(Util.slot(2, 4), ToggleButton.builder()
                .initial(showingTooltip)
                .label(Component.translatable("jsst.itemEditor.attribute.showInTooltip"))
                .disabled(Items.ENDER_PEARL.getDefaultInstance())
                .enabled(Items.ENDER_EYE.getDefaultInstance())
                .setCallback(shouldShowTooltip -> {
                    Sounds.click(player);
                    if (shouldShowTooltip) {
                        Util.unhideTooltipPart(this.stack, ItemStack.TooltipPart.MODIFIERS);
                    } else {
                        this.stack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
                    }
                    this.redraw();
                })
                .build());
    }

    private enum AttributeSlot {
        ANY(null,
                Component.translatable("jsst.itemEditor.attribute.slot.any"),
                JSSTElementBuilder.ui(Items.EMERALD)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.any"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        MAINHAND(EquipmentSlot.MAINHAND,
                Component.translatable("jsst.itemEditor.attribute.slot.mainhand"),
                JSSTElementBuilder.ui(Items.GOLDEN_SWORD)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.mainhand"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        OFFHAND(EquipmentSlot.OFFHAND,
                Component.translatable("jsst.itemEditor.attribute.slot.offhand"),
                JSSTElementBuilder.ui(Items.SHIELD)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.offhand"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        HEAD(EquipmentSlot.HEAD,
                Component.translatable("jsst.itemEditor.attribute.slot.head"),
                JSSTElementBuilder.ui(Items.GOLDEN_HELMET)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.head"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        CHEST(EquipmentSlot.CHEST,
                Component.translatable("jsst.itemEditor.attribute.slot.chest"),
                JSSTElementBuilder.ui(Items.GOLDEN_CHESTPLATE)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.chest"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        LEGS(EquipmentSlot.LEGS,
                Component.translatable("jsst.itemEditor.attribute.slot.legs"),
                JSSTElementBuilder.ui(Items.GOLDEN_LEGGINGS)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.legs"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack()),
        FEET(EquipmentSlot.FEET,
                Component.translatable("jsst.itemEditor.attribute.slot.feet"),
                JSSTElementBuilder.ui(Items.GOLDEN_BOOTS)
                        .setName(Component.translatable("jsst.itemEditor.attribute.slot.feet"))
                        .hideFlags()
                        .dontCleanText()
                        .asStack());

        private final EquipmentSlot slot;
        private final Component name;
        private final ItemStack label;

        AttributeSlot(@Nullable EquipmentSlot slot,
                      Component name,
                      ItemStack label) {
            this.slot = slot;
            this.name = name;
            this.label = label;

            if (slot != null) BY_EQUIPMENT.put(slot, this);
        }
    }

    private record AttributeModInstance(AttributeSlot slot, Attribute attribute, AttributeModifier.Operation operation,
                                        double amount) {

    }


}
