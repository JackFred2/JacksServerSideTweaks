package red.jackf.jsst.feature.beaconpowers;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.sgui.*;

import java.util.List;

public class ExpandedBeaconScreen extends SimpleGui {
    private static final int MAX_POWER = 4;
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
    private final BeaconBlockEntity beacon;
    @Nullable
    private MobEffect primary;
    @Nullable
    private MobEffect secondary;

    public ExpandedBeaconScreen(ServerPlayer player, BeaconBlockEntity beacon) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(Component.translatable("container.beacon"));

        this.beacon = beacon;
        this.primary = BeaconBlockEntityDuck.getPrimaryPower(beacon);
        this.secondary = BeaconBlockEntityDuck.getSecondaryPower(beacon);
    }

    @Override
    public void onTick() {
        // emulate stillvalid
        if (this.beacon.isRemoved()) {
            this.close();
            return;
        }

        if (this.beacon.getLevel().getGameTime() % 80L == 1) {
            this.drawDynamic();
        }

        // update button
        if (this.paymentInv.hasAnyMatching(stack -> stack.is(ItemTags.BEACON_PAYMENT_ITEMS))) {
            this.setSlot(GuiUtil.slot(2, 5), GuiElementBuilder.from(new ItemStack(Items.LIME_CONCRETE))
                                                              .setName(Component.translatable("jsst.common.confirm"))
                                                              .addLoreLine(Hints.leftClick())
                                                              .setCallback(this::confirmUpdate));
        } else {
            this.setSlot(GuiUtil.slot(2, 5), CommonLabels.simple(Items.GRAY_CONCRETE, Component.translatable("jsst.common.confirm")));
        }
    }

    private void confirmUpdate() {

    }

    private void openPrimary() {
        List<MobEffect> options = BuiltInRegistries.MOB_EFFECT.stream().toList();
        Menus.selector(player, Component.translatable("block.minecraft.beacon.primary"), options, LabelMaps.MOB_EFFECTS, selection -> {
            if (selection.hasResult()) this.primary = selection.result();
            this.open();
        });
    }

    private void openSecondary() {
        List<MobEffect> options = BuiltInRegistries.MOB_EFFECT.stream().toList();
        Menus.selector(player, Component.translatable("block.minecraft.beacon.secondary"), options, LabelMaps.MOB_EFFECTS, selection -> {
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
        this.setSlotRedirect(GuiUtil.slot(1, 5), new PaymentSlot(this.paymentInv, 0));
        this.setSlot(GuiUtil.slot(4, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(6, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(7, 5), CommonLabels.divider());
        this.setSlot(GuiUtil.slot(8, 5), CommonLabels.close(this::close));
    }

    private void drawDynamic() {
        final int beaconLevel = BeaconBlockEntityDuck.getPowerLevel(beacon);

        // power level bar
        // TODO add labels saying what you unlock in lore for each level
        for (int level = 0; level < 6; level++) {
            ItemStack label;
            if (level < MAX_POWER) {
                if (level < beaconLevel) {
                    label = CommonLabels.simple(Items.LIME_STAINED_GLASS_PANE, Component.translatable("jsst.beaconpowers.beacon_level", beaconLevel, MAX_POWER));
                } else {
                    label = CommonLabels.simple(Items.RED_STAINED_GLASS_PANE, Component.translatable("jsst.beaconpowers.beacon_level", beaconLevel, MAX_POWER));
                }
            } else {
                label = CommonLabels.simple(Items.BLUE_STAINED_GLASS_PANE, CommonComponents.EMPTY);
            }
            this.setSlot(GuiUtil.slot(4, 5 - level), GuiElementBuilder.from(label));
        }

        // power change buttons
        if (beaconLevel >= 1) {
            GuiUtil.fill(this, ItemStack.EMPTY, 0, 3, 0, 4);
            this.setSlot(GuiUtil.slot(1, 1), CommonLabels.simple(Items.APPLE, Component.translatable("block.minecraft.beacon.primary")));
            this.setSlot(GuiUtil.slot(1, 2), GuiElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(primary))
                                                              .addLoreLine(Hints.leftClick(Component.translatable("jsst.common.change")))
                                                              .setCallback(this::openPrimary));
        } else {
            GuiUtil.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconpowers.beacon_requirement_primary"))
                                           .getItemStack(), 0, 3, 0, 4);
        }

        if (beaconLevel >= SECONDARY_MINIMUM) {
            GuiUtil.fill(this, ItemStack.EMPTY, 6, 9, 0, 4);
            this.setSlot(GuiUtil.slot(7, 1), CommonLabels.simple(Items.GOLDEN_APPLE, Component.translatable("block.minecraft.beacon.secondary")));
            this.setSlot(GuiUtil.slot(7, 2), GuiElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(secondary))
                                                              .addLoreLine(Hints.leftClick(Component.translatable("jsst.common.change")))
                                                              .setCallback(this::openSecondary));
        } else {
            GuiUtil.fill(this, CommonLabels.disabled(Component.translatable("jsst.beaconpowers.beacon_requirement_secondary", MAX_POWER))
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
