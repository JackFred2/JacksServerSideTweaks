package red.jackf.jsst.feature.beaconenhancement;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
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
    @Nullable
    private MobEffect primary;
    @Nullable
    private MobEffect secondary;

    public ExpandedBeaconScreen(ServerPlayer player, BeaconBlockEntity beacon) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(Component.translatable("container.beacon"));

        this.level = player.serverLevel();
        this.beacon = beacon;
        this.primary = BeaconBlockEntityDuck.getPrimaryPower(beacon);
        this.secondary = BeaconBlockEntityDuck.getSecondaryPower(beacon);
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
        }

        // update button
        if (this.hasPaymentItem() && this.hasDifferentResults() && this.primary != null) {
            this.setSlot(GuiUtil.slot(2, 5), GuiElementBuilder.from(new ItemStack(Items.LIME_CONCRETE))
                                                              .setName(Component.translatable("jsst.common.confirm"))
                                                              .addLoreLine(Hints.leftClick())
                                                              .setCallback(this::confirmUpdate));
        } else {
            this.setSlot(GuiUtil.slot(2, 5), CommonLabels.simple(Items.GRAY_CONCRETE, Component.translatable("jsst.common.confirm")));
        }
    }

    private boolean hasPaymentItem() {
        return this.paymentInv.hasAnyMatching(stack -> stack.is(ItemTags.BEACON_PAYMENT_ITEMS));
    }

    private boolean hasDifferentResults() {
        return this.primary != BeaconBlockEntityDuck.getPrimaryPower(this.beacon) || this.secondary != BeaconBlockEntityDuck.getSecondaryPower(this.beacon);
    }

    private void confirmUpdate() {
        if (!this.hasPaymentItem() || !this.hasDifferentResults() || this.primary == null) return;

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

        Menus.selector(player, Component.translatable("block.minecraft.beacon.secondary"), options, map, selection -> {
            if (selection.hasResult()) this.secondary = selection.result();
            this.open();
        });
    }

    private void drawStatic() {
        for (int row = 0; row < 6; row++) {
            this.setSlot(GuiUtil.slot(3, row), CommonLabels.divider());
            this.setSlot(GuiUtil.slot(5, row), CommonLabels.divider());
        }

        for (int col = 0; col < 4; col++) {
            this.setSlot(GuiUtil.slot(col, 4), CommonLabels.divider());
        }

        for (int col = 5; col < 9; col++) {
            this.setSlot(GuiUtil.slot(col, 4), CommonLabels.divider());
        }

        var paymentBuilder = new AnimatedGuiElementBuilder().setInterval(20);
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.BEACON_PAYMENT_ITEMS)) {
            paymentBuilder.setItem(item.value())
                          .setName(Component.translatable("jsst.beaconpowers.beacon_payment"))
                          .saveItemStack();
        }
        this.setSlot(GuiUtil.slot(0, 5), paymentBuilder.build());
        this.setSlotRedirect(GuiUtil.slot(1, 5), this.paymentSlot);
        this.setSlot(GuiUtil.slot(4, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(6, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(7, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(8, 5), CommonLabels.close(this::close));
    }

    private void drawDynamic() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        // power level bar
        for (int level = 1; level <= 6; level++) {
            ItemStack label;
            if (level <= BeaconEnhancement.INSTANCE.config().maxBeaconLevel) {
                boolean active = level <= beaconLevel;
                Component title = Component.translatable(active ? "jsst.beaconpowers.beacon_level_active" : "jsst.beaconpowers.beacon_level_inactive", level)
                        .setStyle(Style.EMPTY.withColor(active ? 0x7FFF7F : 0xFF7F7F));
                var builder = GuiElementBuilder.from(new ItemStack(active ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE))
                                               .setName(title);
                for (MobEffect effect : BeaconEnhancement.INSTANCE.config().powers.getAtLevel(level)) {
                    builder.addLoreLine(Component.empty().withStyle(Styles.LIST_ITEM).append(effect.getDisplayName()));
                }

                label = builder.asStack();
            } else {
                label = CommonLabels.simple(Items.BLUE_STAINED_GLASS_PANE, CommonComponents.EMPTY);
            }
            this.setSlot(GuiUtil.slot(4, 6 - level), GuiElementBuilder.from(label));
        }

        // primary power
        if (beaconLevel >= 1) {
            GuiUtil.fill(this, ItemStack.EMPTY, 0, 3, 0, 4);
            this.setSlot(GuiUtil.slot(1, 1), CommonLabels.simple(Items.APPLE, Component.translatable("block.minecraft.beacon.primary")));
            this.setSlot(GuiUtil.slot(1, 2), GuiElementBuilder.from(primary != null ? LabelMaps.MOB_EFFECTS.getLabel(primary) : NO_EFFECT)
                                                              .addLoreLine(Hints.leftClick(Component.translatable("jsst.common.change")))
                                                              .setCallback(this::openPrimary));
        } else {
            GuiUtil.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconpowers.beacon_requirement", 1))
                                           .getItemStack(), 0, 3, 0, 4);
        }

        // secondary power
        if (beaconLevel >= SECONDARY_MINIMUM) {
            GuiUtil.fill(this, ItemStack.EMPTY, 6, 9, 0, 4);
            this.setSlot(GuiUtil.slot(7, 1), CommonLabels.simple(Items.GOLDEN_APPLE, Component.translatable("block.minecraft.beacon.secondary")));

            ItemStack label;
            if (secondary == null) {
                label = NO_EFFECT;
            } else if (secondary == primary) {
                label = getSecondTierLabel(secondary);
            } else {
                label = LabelMaps.MOB_EFFECTS.getLabel(secondary);
            }
            this.setSlot(GuiUtil.slot(7, 2), GuiElementBuilder.from(label)
                                                              .addLoreLine(Hints.leftClick(Component.translatable("jsst.common.change")))
                                                              .setCallback(this::openSecondary));
        } else {
            GuiUtil.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconpowers.beacon_requirement", SECONDARY_MINIMUM))
                                           .getItemStack(), 6, 9, 0, 4);
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();

        this.drawStatic();
        this.drawDynamic();
    }

    @Override
    public void onClose() {
        super.onClose();
        GuiUtil.returnItems(player, paymentInv);
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
