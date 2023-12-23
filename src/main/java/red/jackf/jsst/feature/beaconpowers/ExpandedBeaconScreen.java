package red.jackf.jsst.feature.beaconpowers;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.util.gui.CommonLabels;
import red.jackf.jsst.util.gui.GuiUtil;
import red.jackf.jsst.util.gui.LabelMaps;
import red.jackf.jsst.util.gui.Menus;

import java.util.List;

public class ExpandedBeaconScreen extends SimpleGui {
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
        super(MenuType.GENERIC_9x1, player, false);
        this.setTitle(Component.translatable("container.beacon"));

        this.beacon = beacon;
        this.primary = BeaconBlockEntityDuck.getPrimaryPower(beacon);
        this.secondary = BeaconBlockEntityDuck.getSecondaryPower(beacon);
    }

    private void openPrimary() {
        List<MobEffect> options = BuiltInRegistries.MOB_EFFECT.stream().toList();
        Menus.selector(player, options, LabelMaps.MOB_EFFECTS, selection -> {
            if (selection.hasResult()) this.primary = selection.result();
            this.open();
        });
    }

    private void openSecondary() {
        List<MobEffect> options = BuiltInRegistries.MOB_EFFECT.stream().toList();
        Menus.selector(player, options, LabelMaps.MOB_EFFECTS, selection -> {
            if (selection.hasResult()) this.secondary = selection.result();
            this.open();
        });
    }

    @Override
    public void onOpen() {
        super.onOpen();

        this.setSlot(0, GuiElementBuilder.from(new ItemStack(Items.BEACON)));
        this.setSlot(1, CommonLabels.divider());
        this.setSlot(2, GuiElementBuilder.from(new ItemStack(Items.APPLE)).setCallback(this::openPrimary));
        this.setSlot(3, GuiElementBuilder.from(new ItemStack(Items.GOLDEN_APPLE)).setCallback(this::openSecondary));
        this.setSlot(4, CommonLabels.divider());
        this.setSlotRedirect(6, new PaymentSlot(paymentInv, 0));
        this.setSlot(5, GuiElementBuilder.from(new ItemStack(Items.GRAY_CONCRETE)));
        this.setSlot(7, CommonLabels.divider());
        this.setSlot(8, CommonLabels.close(this::close));
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
