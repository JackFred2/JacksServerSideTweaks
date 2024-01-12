package red.jackf.jsst.feature.beaconenhancement;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.Streams;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.labels.LabelMap;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpandedBeaconScreen extends SimpleGui {
    private static final ItemStack NO_EFFECT = GuiElementBuilder.from(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER))
                                                                .setName(Component.translatable("effect.none"))
                                                                .hideFlags()
                                                                .asStack();
    private static final int SECONDARY_MINIMUM = 4;
    private final Container paymentInv = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private final PaymentSlot paymentSlot = new PaymentSlot(this.paymentInv, 0);

    private final BeaconBlockEntity beacon;
    private final ServerLevel level;
    private boolean lastWasMax;
    @Nullable
    private MobEffect primary;
    @Nullable
    private MobEffect secondary;

    public ExpandedBeaconScreen(ServerPlayer player, BeaconBlockEntity beacon) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(beacon.getDisplayName());

        this.level = player.serverLevel();
        this.beacon = beacon;
        this.primary = BeaconBlockEntityDuck.getPrimaryPower(beacon);
        this.secondary = BeaconBlockEntityDuck.getSecondaryPower(beacon);
        this.lastWasMax = BeaconBlockEntityDuck.getPowerLevel(this.beacon) == BeaconEnhancement.INSTANCE.config().maxBeaconLevel;
    }

    private static ItemStack getSecondTierLabel(MobEffect effect) {
        return GuiElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(effect))
                                .setName(Component.translatable("potion.withAmplifier",
                                                                effect.getDisplayName(),
                                                                Component.translatable("potion.potency.1")))
                                .asStack();
    }

    @Override
    public void onTick() {
        // emulate stillvalid
        if (this.beacon.isRemoved()) {
            this.close();
            return;
        }

        if (this.level.getGameTime() % 80L == 1) {
            this.drawDynamic();
            boolean isMax = BeaconBlockEntityDuck.getPowerLevel(beacon) == BeaconEnhancement.INSTANCE.config().maxBeaconLevel;
            if (isMax != lastWasMax) {
                this.drawBeaconOutline();
                this.lastWasMax = isMax;
            }
        }

        GuiElementBuilder item;

        if (this.hasNoChanges()) {
            item = GuiElementBuilder.from(CommonLabels.simple(Items.GRAY_CONCRETE, Component.translatable("jsst.common.noChanges").withStyle(Styles.NEGATIVE)));
        } else if (this.primary == null) {
            item = GuiElementBuilder.from(CommonLabels.simple(Items.GRAY_CONCRETE, Component.translatable("jsst.beaconPowers.error.needsPrimary").withStyle(Styles.NEGATIVE)));
        } else if (this.hasNoPayment()) {
            item = GuiElementBuilder.from(CommonLabels.simple(Items.GRAY_CONCRETE, Component.translatable("jsst.beaconPowers.error.beaconPayment").withStyle(Styles.NEGATIVE)));
        } else {
            item = GuiElementBuilder.from(new ItemStack(Items.LIME_CONCRETE))
                                    .setName(Hints.leftClick(Translations.confirm()))
                                    .setCallback(Inputs.leftClick(this::confirmUpdate));
        }
        this.setSlot(Util.slot(2, 5), item);
    }

    private boolean hasNoPayment() {
        return !this.paymentInv.hasAnyMatching(stack -> stack.is(ItemTags.BEACON_PAYMENT_ITEMS));
    }

    private boolean hasNoChanges() {
        return this.primary == BeaconBlockEntityDuck.getPrimaryPower(this.beacon) && this.secondary == BeaconBlockEntityDuck.getSecondaryPower(this.beacon);
    }

    private void confirmUpdate() {
        if (this.hasNoPayment() || this.hasNoChanges() || this.primary == null) return;

        if (!this.level.isClientSide && !this.beacon.getBeamSections().isEmpty()) {
            BeaconBlockEntity.playSound(this.level, this.beacon.getBlockPos(), SoundEvents.BEACON_POWER_SELECT);
        }

        BeaconBlockEntityDuck.setPrimaryPower(this.beacon, this.primary);
        BeaconBlockEntityDuck.setSecondaryPower(this.beacon, this.secondary);
        this.paymentSlot.remove(1);
        this.close();
    }

    private void openPrimary() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        List<MobEffect> options = BeaconEnhancement.INSTANCE.config().powers.getPrimaries(beaconLevel);
        options.sort(Streams.comparingComponent(MobEffect::getDisplayName));

        Sounds.click(player);
        Menus.selector(player, Component.translatable("block.minecraft.beacon.primary"), options, LabelMaps.MOB_EFFECTS, selection -> {
            if (selection.hasResult()) this.primary = selection.result();
            this.open();
        });
    }

    private void openSecondary() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        List<MobEffect> options = new ArrayList<>(BeaconEnhancement.INSTANCE.config().powers.getSecondaries(beaconLevel));
        options.sort(Streams.comparingComponent(MobEffect::getDisplayName));

        LabelMap<MobEffect> map = LabelMaps.MOB_EFFECTS;
        if (this.primary != null) {
            map = map.withAdditional(Map.of(
                    this.primary,
                    getSecondTierLabel(this.primary)
            ));
            options.add(0, this.primary);
        }

        Sounds.click(player);
        Menus.selector(player, Component.translatable("block.minecraft.beacon.secondary"), options, map, selection -> {
            if (selection.hasResult()) this.secondary = selection.result();
            this.open();
        });
    }

    private static GuiElementInterface createPowered(int offset) {
        final int SPACING = 3;

        var builder = new AnimatedGuiElementBuilder();
        builder.setInterval(8);

        offset = Mth.positiveModulo(offset, SPACING);

        for (int i = 0; i < SPACING; i++) {
            builder.setItem(i == offset ? Items.MAGENTA_STAINED_GLASS_PANE : Items.PURPLE_STAINED_GLASS_PANE);
            builder.setName(Component.empty());
            builder.saveItemStack();
        }

        return builder.build();
    }

    private void drawStatic() {
        var paymentBuilder = new AnimatedGuiElementBuilder().setInterval(20);
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.BEACON_PAYMENT_ITEMS)) {
            paymentBuilder.setItem(item.value()).saveItemStack();
        }
        this.setSlot(Util.slot(0, 5), paymentBuilder.build());
        this.setSlotRedirect(Util.slot(1, 5), this.paymentSlot);
        this.setSlot(Util.slot(3, 5), CommonLabels.divider());
        this.setSlot(Util.slot(4, 5), CommonLabels.divider());
        this.setSlot(Util.slot(5, 5), CommonLabels.divider());
        this.setSlot(Util.slot(6, 5), CommonLabels.divider());
        this.setSlot(Util.slot(7, 5), CommonLabels.divider());
        this.setSlot(Util.slot(8, 5), CommonLabels.close(() -> {
            Sounds.close(player);
            this.close();
        }));
    }

    private void drawBeaconOutline() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        // beacon borders
        if (beaconLevel == BeaconEnhancement.INSTANCE.config().maxBeaconLevel) {
            for (int row = 0; row < 5; row++) {
                this.setSlot(Util.slot(3, row), createPowered(7 - row));
                this.setSlot(Util.slot(5, row), createPowered(7 - row));
            }

            for (int col = 0; col < 4; col++) {
                this.setSlot(Util.slot(col, 4), createPowered(col));
            }

            for (int col = 5; col < 9; col++) {
                this.setSlot(Util.slot(col, 4), createPowered(8 - col));
            }
        } else {
            for (int row = 0; row < 5; row++) {
                this.setSlot(Util.slot(3, row), CommonLabels.divider());
                this.setSlot(Util.slot(5, row), CommonLabels.divider());
            }

            for (int col = 0; col < 4; col++) {
                this.setSlot(Util.slot(col, 4), CommonLabels.divider());
            }

            for (int col = 5; col < 9; col++) {
                this.setSlot(Util.slot(col, 4), CommonLabels.divider());
            }
        }
    }

    private void drawDynamic() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        // power level bar
        for (int level = 1; level <= 6; level++) {
            ItemStack label;
            if (level <= BeaconEnhancement.INSTANCE.config().maxBeaconLevel) {
                boolean active = level <= beaconLevel;
                Component title = Component.translatable(active ? "jsst.beaconPowers.beaconLevelActive" : "jsst.beaconPowers.beaconLevelInactive", level)
                        .setStyle(active ? Styles.POSITIVE : Styles.NEGATIVE);
                var builder = GuiElementBuilder.from(new ItemStack(active ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE))
                                               .setName(title);
                for (MobEffect effect : BeaconEnhancement.INSTANCE.config().powers.getAtLevel(level)) {
                    builder.addLoreLine(Component.empty().withStyle(Styles.MINOR_LABEL).append(effect.getDisplayName()));
                }

                label = builder.asStack();
            } else {
                label = CommonLabels.simple(Items.BLUE_STAINED_GLASS_PANE, CommonComponents.EMPTY);
            }
            this.setSlot(Util.slot(4, 6 - level), GuiElementBuilder.from(label));
        }

        // primary power
        if (beaconLevel >= 1) {
            Util.fill(this, ItemStack.EMPTY, 0, 3, 0, 4);
            this.setSlot(Util.slot(1, 1), CommonLabels.simple(Items.APPLE, Component.translatable("block.minecraft.beacon.primary")));
            this.setSlot(Util.slot(1, 2), GuiElementBuilder.from(primary != null ? LabelMaps.MOB_EFFECTS.getLabel(primary) : NO_EFFECT)
                                                           .addLoreLine(Hints.leftClick(Translations.change()))
                                                           .setCallback(Inputs.leftClick(this::openPrimary)));
        } else {
            Util.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconPowers.beaconRequirement", 1))
                                        .getItemStack(), 0, 3, 0, 4);
        }

        // secondary power
        if (beaconLevel >= SECONDARY_MINIMUM) {
            Util.fill(this, ItemStack.EMPTY, 6, 9, 0, 4);
            this.setSlot(Util.slot(7, 1), CommonLabels.simple(Items.GOLDEN_APPLE, Component.translatable("block.minecraft.beacon.secondary")));

            ItemStack label;
            if (secondary == null) {
                label = NO_EFFECT;
            } else if (secondary == primary) {
                label = getSecondTierLabel(secondary);
            } else {
                label = LabelMaps.MOB_EFFECTS.getLabel(secondary);
            }
            this.setSlot(Util.slot(7, 2), GuiElementBuilder.from(label)
                                                           .addLoreLine(Hints.leftClick(Translations.change()))
                                                           .setCallback(Inputs.leftClick(this::openSecondary)));
        } else {
            Util.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconPowers.beaconRequirement", SECONDARY_MINIMUM))
                                        .getItemStack(), 6, 9, 0, 4);
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();

        this.drawStatic();
        this.drawBeaconOutline();
        this.drawDynamic();
    }

    @Override
    public void onClose() {
        super.onClose();
        Util.returnItems(player, paymentInv);
    }

    private static class PaymentSlot extends Slot {
        private PaymentSlot(Container container, int slot) {
            super(container, slot, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
