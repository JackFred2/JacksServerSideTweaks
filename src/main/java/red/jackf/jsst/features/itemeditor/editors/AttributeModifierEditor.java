package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.*;

import java.util.*;
import java.util.function.Consumer;

public class AttributeModifierEditor extends Editor {
    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();
    private static final String TAG_ATTRIBUTE_MODIFIERS = "AttributeModifiers";
    private static final int MAX_MODIFIERS = 20;
    private final List<AttributeModInstance> modifiers = new ArrayList<>();
    private int page = 0;
    public AttributeModifierEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        parseStack();
    }

    private void parseStack() {
        this.modifiers.clear();
        var tag = stack.getTag();
        if (tag != null && tag.contains(TAG_ATTRIBUTE_MODIFIERS, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(TAG_ATTRIBUTE_MODIFIERS, Tag.TAG_COMPOUND);

            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag attributeTag = listTag.getCompound(i);
                Optional<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(attributeTag.getString("AttributeName")));
                if (attribute.isPresent()) {
                    var slot = attributeTag.contains("Slot", 8) ? EquipmentSlot.byName(attributeTag.getString("Slot")) : null;
                    var modifier = AttributeModifier.load(attributeTag);
                    if (modifier != null && modifier.getId().getLeastSignificantBits() != 0L && modifier.getId().getMostSignificantBits() != 0L) {
                        modifiers.add(new AttributeModInstance(attribute.get(), modifier, slot));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack label() {
        var stack = new ItemStack(Items.STICK);
        stack.enchant(Enchantments.AQUA_AFFINITY, 1);
        return Labels.create(stack).withName("Edit Attribute Modifiers").build();
    }

    private ItemStack build() {
        var baseStack = getOriginal();
        baseStack.removeTagKey(TAG_ATTRIBUTE_MODIFIERS);
        this.modifiers.forEach(instance -> baseStack.addAttributeModifier(instance.attribute, instance.modifier, instance.slot));
        return baseStack;
    }

    private void reset() {
        this.stack = getOriginal();
        parseStack();
        open();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();

        elements.put(10, new ItemGuiElement(Labels.create(build()).keepLore().withHint("Click to finish").build(), () -> {
            this.stack = build();
            complete();
        }));

        elements.put(45, new ItemGuiElement(Labels.create(Items.GRINDSTONE).withName("Clear").build(), () -> {
            Sounds.grind(player);
            this.modifiers.clear();
            open();
        }));
        elements.put(46, EditorUtils.reset(this::reset));
        elements.put(47, EditorUtils.cancel(this::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        var maxPage = (modifiers.size() / 5) - (modifiers.size() >= MAX_MODIFIERS ? 1 : 0);
        this.page = Mth.clamp(this.page, 0, maxPage);
        EditorUtils.drawPage(elements, this.modifiers, this.page, maxPage, newPage -> {
            this.page = newPage;
            Sounds.page(player, page, maxPage);
            open();
        }, 6, (slot, index) -> {
            var instance = modifiers.get(index);

            // Main Icon
            var label = Labels.create(LabelData.ATTRIBUTES.get(instance.attribute))
                    .withName(Component.translatable(instance.attribute.getDescriptionId()).withStyle(Labels.CLEAN));
            var amount = instance.modifier.getAmount() * (instance.modifier.getOperation() == AttributeModifier.Operation.ADDITION ? (instance.attribute == Attributes.KNOCKBACK_RESISTANCE ? 10 : 1) : 100);
            if (instance.slot != null) label.withHint(Component.translatable("item.modifiers." + instance.slot.getName()).withStyle(Labels.HINT));
            label.withHint(Component.translatable("attribute.modifier." + (amount >= 0 ? "plus." : "take.") + instance.modifier.getOperation().toValue(),
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(amount)), Component.translatable(instance.attribute.getDescriptionId())).withStyle(amount >= 0 ? Labels.HINT : Labels.WARNING));
            label.withHint("Click to change modifier");
            elements.put(slot, new ItemGuiElement(label.build(), () -> {
                Sounds.interact(player);
                Menus.attribute(player, CancellableCallback.of(attribute -> {
                    Sounds.success(player);
                    modifiers.set(index, new AttributeModInstance(attribute, instance.modifier, instance.slot));
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));

            // Amount editor
            elements.put(slot + 1, new ItemGuiElement(Labels.create(Items.CLOCK).withName("Edit Amount").build(), () -> {
                Sounds.interact(player);
                Menus.decimal(player, instance.modifier.getAmount(), CancellableCallback.of(newAmount -> {
                    Sounds.success(player);
                    modifiers.set(index, new AttributeModInstance(instance.attribute, newAmount, instance.modifier.getOperation(), instance.slot));
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));

            // Operator selector
            elements.put(slot + 2, Selector.create(AttributeOperator.class, "Modifier Mode", operatorReverseMap.get(instance.modifier.getOperation()), newOp -> {
                Sounds.interact(player);
                modifiers.set(index, new AttributeModInstance(instance.attribute, instance.modifier.getAmount(), newOp.trueValue, instance.slot));
                open();
            }));

            // Slot selector
            elements.put(slot + 3, Selector.create(AttributeSlot.class, "Active Slot", slotReverseMap.get(instance.slot), newSlot -> {
                Sounds.interact(player);
                modifiers.set(index, new AttributeModInstance(instance.attribute, instance.modifier, newSlot.trueSlot));
                open();
            }));
        }, index -> {
            Sounds.error(player);
            modifiers.remove((int) index);
            open();
        }, () -> {
            Sounds.success(player);
            modifiers.add(new AttributeModInstance(Attributes.MAX_HEALTH, 2.0, AttributeModifier.Operation.ADDITION, EquipmentSlot.MAINHAND));
            open();
        });

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Attribute Modifiers"), elements));
    }

    private static String getRandomName(Attribute attribute) {
        var key = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        return "jsst_attribute_" + (key != null ? key.toString() : "unknown") + "_" + RANDOM.nextLong();
    }

    private record AttributeModInstance(Attribute attribute, AttributeModifier modifier, @Nullable EquipmentSlot slot) {
        public AttributeModInstance(Attribute attribute, double value, AttributeModifier.Operation operation, @Nullable EquipmentSlot slot) {
            this(attribute, new AttributeModifier(getRandomName(attribute), value, operation), slot);
        }
    }

    // don't want to glue Labeled to the vanilla enums
    private static final Map<AttributeModifier.Operation, AttributeOperator> operatorReverseMap = Map.of(
            AttributeModifier.Operation.ADDITION, AttributeOperator.ADDITIVE,
            AttributeModifier.Operation.MULTIPLY_BASE, AttributeOperator.MULTIPLICATIVE,
            AttributeModifier.Operation.MULTIPLY_TOTAL, AttributeOperator.EXPONENTIAL
    );

    private enum AttributeOperator implements Selector.Labeled {
        ADDITIVE(new ItemStack(Items.SUGAR), "Additive", AttributeModifier.Operation.ADDITION),
        MULTIPLICATIVE(new ItemStack(Items.REDSTONE), "Multiplicative", AttributeModifier.Operation.MULTIPLY_BASE),
        EXPONENTIAL(new ItemStack(Items.REDSTONE_BLOCK), "Exponential", AttributeModifier.Operation.MULTIPLY_TOTAL);

        private final ItemStack label;
        private final String settingName;
        private final AttributeModifier.Operation trueValue;

        AttributeOperator(ItemStack label, String settingName, AttributeModifier.Operation trueValue) {
            this.label = label;
            this.settingName = settingName;
            this.trueValue = trueValue;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }

    private static final Map<EquipmentSlot, AttributeSlot> slotReverseMap = new HashMap<>();
    static {
        slotReverseMap.put(EquipmentSlot.MAINHAND, AttributeSlot.MAIN_HAND);
        slotReverseMap.put(EquipmentSlot.OFFHAND, AttributeSlot.OFF_HAND);
        slotReverseMap.put(EquipmentSlot.HEAD, AttributeSlot.HEAD);
        slotReverseMap.put(EquipmentSlot.CHEST, AttributeSlot.CHEST);
        slotReverseMap.put(EquipmentSlot.LEGS, AttributeSlot.LEGS);
        slotReverseMap.put(EquipmentSlot.FEET, AttributeSlot.FEET);
        slotReverseMap.put(null, AttributeSlot.ALL);
    }

    private enum AttributeSlot implements Selector.Labeled {
        MAIN_HAND(new ItemStack(Items.DIAMOND_SWORD), "Main Hand", EquipmentSlot.MAINHAND),
        OFF_HAND(new ItemStack(Items.SHIELD), "Off Hand", EquipmentSlot.OFFHAND),
        HEAD(new ItemStack(Items.DIAMOND_HELMET), "Head", EquipmentSlot.HEAD),
        CHEST(new ItemStack(Items.DIAMOND_CHESTPLATE), "Chest", EquipmentSlot.CHEST),
        LEGS(new ItemStack(Items.DIAMOND_LEGGINGS), "Legs", EquipmentSlot.LEGS),
        FEET(new ItemStack(Items.DIAMOND_BOOTS), "Feet", EquipmentSlot.FEET),
        ALL(new ItemStack(Items.NETHER_STAR), "Any", null);
        private final ItemStack label;
        private final String settingName;
        private final EquipmentSlot trueSlot;

        AttributeSlot(ItemStack label, String settingName, @Nullable EquipmentSlot trueSlot) {
            this.label = label;
            this.settingName = settingName;
            this.trueSlot = trueSlot;
        }

        @Override
        public ItemStack label() {
            return label;
        }

        @Override
        public String settingName() {
            return settingName;
        }
    }
}
