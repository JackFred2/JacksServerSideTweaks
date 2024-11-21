package red.jackf.jsst.impl.feature.beaconenhancement;

import com.google.common.collect.Multimap;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.CommonElements;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.AnimatedGuiElementBuilderExt;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;
import red.jackf.jsst.impl.utils.sgui.labels.LabelMaps;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;
import red.jackf.jsst.impl.utils.sgui.region.UIRegion;
import red.jackf.jsst.mixins.beaconenhancement.BeaconBlockEntityAccessor;

import java.util.ArrayList;
import java.util.List;

/*
TODO
- conduit range
- cleanup

 */

public class AltBeaconMenu extends SimpleGuiExt {
    private final BeaconBlockEntityAccessor bbeAccessor;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData dataAccess;
    private int levels;

    private final UIRegion primaryArea = UIRegion.rectangle(this, 0, 0, 3, 4);
    private final UIRegion secondaryArea = UIRegion.rectangle(this, 6, 0, 9, 4);

    private @Nullable Holder<MobEffect> primary;
    private @Nullable Holder<MobEffect> secondary;

    private final Container payment = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            AltBeaconMenu.this.redrawConfirm();
        }
    };
    private final Slot paymentSlot = new Slot(payment, 0, 0, 0) {
        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };

    public AltBeaconMenu(ServerPlayer player, BeaconBlockEntity bbe, ContainerLevelAccess levelAccess) {
        super(MenuType.GENERIC_9x6, player, false);
        this.bbeAccessor = (BeaconBlockEntityAccessor) bbe;
        this.dataAccess = bbeAccessor.getDataAccess();
        this.levelAccess = levelAccess;
        this.primary = bbeAccessor.getPrimaryPower();
        this.secondary = bbeAccessor.getSecondaryPower();
        this.levels = bbeAccessor.getLevels();
        this.setTitle(bbe.getDisplayName());
        this.drawStatic();
    }

    @Override
    protected void drawStatic() {
        this.setSlot(8, 5, CommonElements.close(this::close));

        List<Item> paymentItems = RegistryUtils.streamTag(RegistryUtils.lookup(this.player.registryAccess(), Registries.ITEM), ItemTags.BEACON_PAYMENT_ITEMS)
                .map(Holder::value)
                .toList();

        var paymentLabel = new AnimatedGuiElementBuilderExt().setInterval(20);

        for (Item paymentItem : paymentItems) {
            var builder = JSSTElementBuilder.from(paymentItem).ui()
                    .setName(Component.translatable("jsst.beaconEnhancement.paymentItems"));

            for (Item paymentItem2 : paymentItems) {
                builder.addLoreLine(Component.literal(" - ").withStyle(Styles.LABEL).append(paymentItem2.getDefaultInstance().getHoverName()));
            }

            paymentLabel.addStack(builder.asStack());
        }

        this.setSlot(0, 5, paymentLabel);

        this.setSlotRedirect(1, 5, paymentSlot);
    }

    private JSSTElementBuilder getEffectIcon(@Nullable Holder<MobEffect> effect) {
        if (effect == null) {
            return JSSTElementBuilder.from(Items.GLASS_BOTTLE).ui()
                    .setName(Component.translatable("jsst.beaconEnhancement.noPower"));
        } else {
            return JSSTElementBuilder.from(LabelMaps.MOB_EFFECT.apply(effect)).ui();
        }
    }

    private void redrawConfirm() {
        if (this.levels == 0) {
            this.setSlot(2, 5, JSSTElementBuilder.from(Items.GRAY_CONCRETE).ui()
                    .setName(Component.translatable("jsst.beaconEnhancement.beaconInactive").setStyle(Styles.NEGATIVE)));
        } else if (!this.paymentSlot.hasItem()) {
            this.setSlot(2, 5, JSSTElementBuilder.from(Items.GRAY_CONCRETE).ui()
                    .setName(Component.translatable("jsst.beaconEnhancement.noPaymentItem").setStyle(Styles.NEGATIVE)));
        } else {
            this.setSlot(2, 5, JSSTElementBuilder.from(Items.LIME_CONCRETE).ui()
                    .leftClick(Translations.confirm(), () -> {
                        if (this.paymentSlot.hasItem()) {
                            this.paymentSlot.remove(1);
                            this.dataAccess.set(1, BeaconMenu.encodeEffect(this.primary));
                            this.dataAccess.set(2, BeaconMenu.encodeEffect(this.secondary));
                            this.levelAccess.execute(Level::blockEntityChanged);
                            this.close();
                        }
                    }));
        }
    }

    private static GuiElementInterface staticDivider() {
        return CommonElements.divider(DyeColor.BLUE);
    }

    private static GuiElementInterface animatedDivider(int offset) {
        final int spacing = 4;

        var builder = new AnimatedGuiElementBuilderExt().setInterval(6);

        for (int i = 0; i < spacing; i++) {
            builder.addStack((offset - i) % spacing == 0 ? CommonElements.divider(DyeColor.MAGENTA).getItemStack() : CommonElements.divider(DyeColor.PURPLE).getItemStack());
        }

        return builder.build();
    }

    private void redrawDividers() {
        if (this.levels == 6) {
            for (int i = 0; i < 4; i++) {
                this.setSlot(i, 4, animatedDivider(i));
                this.setSlot(8 - i, 4, animatedDivider(i));

                this.setSlot(3, 3 - i, animatedDivider(i));
                this.setSlot(5, 3 - i, animatedDivider(i));
            }
            this.setSlot(3, 5, CommonElements.divider(DyeColor.PURPLE));
            this.setSlot(5, 5, CommonElements.divider(DyeColor.PURPLE));
        } else {
            UIRegion.row(this, 4, 0, 4).fillElement(AltBeaconMenu::staticDivider);
            UIRegion.row(this, 4, 5, 9).fillElement(AltBeaconMenu::staticDivider);
            UIRegion.column(this, 3, 0, 4).fillElement(AltBeaconMenu::staticDivider);
            UIRegion.column(this, 5, 0, 4).fillElement(AltBeaconMenu::staticDivider);
            this.setSlot(3, 5, staticDivider());
            this.setSlot(5, 5, staticDivider());
        }
    }

    @Override
    protected void refresh() {
        this.redrawDividers();

        JSSTConfig.BeaconEnhancement config = JSSTConfig.INSTANCE.instance().beaconEnhancement;
        Multimap<Integer, Holder<MobEffect>> primaryPowers = config.primaryPowers.parse(this.getPlayer().registryAccess());
        Multimap<Integer, Holder<MobEffect>> secondaryPowers = config.secondaryPowers.parse(this.getPlayer().registryAccess());

        if (this.levels >= 1) {
            this.primaryArea.clearSlots();

            this.setSlot(1, 1, getEffectIcon(primary)
                    .addLoreLine(Component.translatable("jsst.beaconEnhancement.primary").withStyle(Styles.LABEL))
                    .leftClick(Translations.change(), () -> {
                        Sounds.UI.click(player);

                        List<Holder<MobEffect>> effects = new ArrayList<>();

                        for (int i = 1; i <= this.levels; i++) {
                            effects.addAll(primaryPowers.get(i));
                        }

                        SelectionMenu.<Holder<MobEffect>>builder(player)
                                .title(Component.translatable("jsst.beaconEnhancement.primary"))
                                .labelStacks(LabelMaps.MOB_EFFECT)
                                .options(effects)
                                .start(opt -> {
                                    opt.ifPresent(newEffect -> this.primary = newEffect);
                                    this.open();
                                });
                    }));
        } else {
            this.primaryArea.fillElement(() -> CommonElements.disabled(Component.translatable("jsst.beaconEnhancement.beaconInactive", config.secondPowerMinLevel)
                    .withStyle(Styles.NEGATIVE)));
        }

        if (config.enableSecondPower && this.levels >= config.secondPowerMinLevel) {
            this.secondaryArea.clearSlots();

            this.setSlot(7, 1, getEffectIcon(secondary)
                    .addLoreLine(Component.translatable("jsst.beaconEnhancement.secondary").withStyle(Styles.LABEL))
                    .leftClick(Translations.change(), () -> {
                        Sounds.UI.click(player);

                        List<Holder<MobEffect>> effects = new ArrayList<>();

                        if (this.primary != null) {
                            effects.add(this.primary);
                        }

                        for (int i = 1; i <= this.levels; i++) {
                            effects.addAll(secondaryPowers.get(i));
                        }

                        SelectionMenu.<Holder<MobEffect>>builder(player)
                                .title(Component.translatable("jsst.beaconEnhancement.secondary"))
                                .labelStacks(LabelMaps.MOB_EFFECT)
                                .options(effects)
                                .start(opt -> {
                                    opt.ifPresent(newEffect -> this.secondary = newEffect);
                                    this.open();
                                });
                    }));
        } else {
            if (this.levels == 0) {
                this.secondaryArea.fillElement(() -> CommonElements.disabled(Component.translatable("jsst.beaconEnhancement.beaconInactive", config.secondPowerMinLevel)
                        .withStyle(Styles.NEGATIVE)));
            } else {
                this.secondaryArea.fillElement(() -> CommonElements.disabled(Component.translatable("jsst.beaconEnhancement.minLevelForSecondary", config.secondPowerMinLevel)
                        .withStyle(Styles.NEGATIVE)));
            }
        }

        // power beam
        for (int i = 1; i <= 6; i++) {
            int capped = Math.min(JSSTConfig.INSTANCE.instance().beaconEnhancement.maxLevel, i);
            boolean active = this.levels >= capped;

            var builder = JSSTElementBuilder.from(active ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE).ui()
                    .setName(Component.translatable("jsst.beaconEnhancement.level", capped).withStyle(active ? Styles.POSITIVE : Styles.NEGATIVE));

            for (Holder<MobEffect> effect : primaryPowers.get(capped)) {
                builder.addLoreLine(Component.literal(" - ").withStyle(Styles.LABEL).append(effect.value().getDisplayName()));
            }

            for (Holder<MobEffect> effect : secondaryPowers.get(capped)) {
                builder.addLoreLine(Component.literal(" - ").withStyle(Styles.LABEL).append(effect.value().getDisplayName()));
            }

            this.setSlot(4, 6 - i, builder);
        }

        // confirm
        this.redrawConfirm();
    }

    @Override
    public void onClose() {
        super.onClose();

        ItemStack paymentStack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
        if (!paymentStack.isEmpty()) {
            this.player.drop(paymentStack, false);
        }
    }

    @Override
    public void onTick() {
        int currentLevel = bbeAccessor.getLevels();
        if (currentLevel != levels) {
            this.levels = currentLevel;
            this.refresh();
        }
    }
}
