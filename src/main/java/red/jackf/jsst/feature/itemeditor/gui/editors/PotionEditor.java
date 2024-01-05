package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
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
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.labels.LabelMaps;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PotionEditor extends GuiEditor {
    private static final List<Item> POTION_ITEMS = List.of(
            Items.POTION,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.TIPPED_ARROW
    );

    public static final EditorType TYPE = new EditorType(
            PotionEditor::new,
            false,
            stack -> POTION_ITEMS.contains(stack.getItem()),
            PotionEditor::createLabel
    );
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private final ListPaginator<MobEffectInstance> effectPaginator = ListPaginator.<MobEffectInstance>builder(this)
                                                                                  .at(4, 9, 2, 6)
                                                                                  .list(this.effects)
                                                                                  .max(20)
                                                                                  .rowDraw(this::drawPageRow)
                                                                                  .modifiable(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), false)
                                                                                  .onUpdate(this::redraw).build();

    public PotionEditor(
            ServerPlayer player,
            boolean cosmeticOnly,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, cosmeticOnly, initial, callback);
        this.setTitle(Component.translatable("jsst.itemEditor.potionEditor"));
        this.drawStatic();
        this.effects.addAll(PotionUtils.getCustomEffects(this.stack));
    }

    @Override
    protected void reset() {
        super.reset();
        this.effects.clear();
        this.effects.addAll(PotionUtils.getCustomEffects(this.stack));
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

    private static AnimatedGuiElementBuilder createLabel() {
        AnimatedGuiElementBuilderExt builder = new AnimatedGuiElementBuilderExt();

        for (Colour colour : List.of(Colours.RED, Colours.ORANGE, Colours.YELLOW, Colours.GREEN, Colours.LIGHT_BLUE, Colours.BLUE, Colours.PURPLE, Colours.MAGENTA)) {
            builder.addStack(GuiElementBuilder.from(setColour(Items.POTION.getDefaultInstance(), colour))
                                              .hideFlags()
                                              .setName(Component.translatable("jsst.itemEditor.potionEditor"))
                                              .asStack());
        }

        builder.setInterval(20);

        return builder;
    }

    private void updateCustomEffects() {
        if (this.effects.isEmpty()) {
            this.stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_EFFECTS);
        } else {
            PotionUtils.setCustomEffects(this.stack, this.effects);
        }
    }

    private static ItemStack setColour(ItemStack potionItem, Colour colour) {
        potionItem.getOrCreateTag().putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, colour.toARGB());
        return potionItem;
    }

    private void drawStatic() {
        this.setSlot(Util.slot(0, 5), CommonLabels.close(this::close));

        for (int row = 0; row < 6; row++) this.setSlot(Util.slot(3, row), CommonLabels.divider());

        for (int col = 4; col < 9; col++) this.setSlot(Util.slot(col, 1), CommonLabels.divider());

        this.setSlot(Util.slot(0, 4), GuiElementBuilder.from(Items.RED_DYE.getDefaultInstance())
                                                       .setName(Component.translatable("jsst.itemEditor.colour.custom"))
                                                       .addLoreLine(Hints.leftClick(Translations.open()))
                                                       .setCallback(Inputs.leftClick(() -> {
                                                           Sounds.click(player);
                                                           EditorMenus.colour(player, result -> {
                                                               if (result.hasResult())
                                                                   setColour(stack, result.result());
                                                               this.open();
                                                           });
                                                       })));
    }

    @Override
    protected void redraw() {
        this.updateCustomEffects();
        this.drawPreview(Util.slot(1, 1));

        //noinspection DataFlowIssue
        if (this.stack.hasTag() && this.stack.getTag().contains(PotionUtils.TAG_CUSTOM_POTION_COLOR, Tag.TAG_INT)) {
            this.setSlot(Util.slot(1, 4), GuiElementBuilder.from(Items.GUNPOWDER.getDefaultInstance())
                                                           .setName(Component.translatable("jsst.itemEditor.colour.custom.remove").setStyle(Styles.INPUT_HINT))
                                                           .addLoreLine(Hints.leftClick())
                                                           .setCallback(Inputs.leftClick(() -> {
                                                               Sounds.clear(player);
                                                               this.stack.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_COLOR);
                                                               this.redraw();
                                                           })));
        } else {
            this.clearSlot(Util.slot(1, 5));
        }

        this.setSlot(Util.slot(4, 0), GuiElementBuilder.from(LabelMaps.POTIONS.getLabel(PotionUtils.getPotion(this.stack)))
                                                       .setName(Component.translatable("jsst.itemEditor.potionEditor.setPotion").setStyle(Styles.INPUT_HINT))
                                                       .addLoreLine(Hints.leftClick())
                                                       .setCallback(Inputs.leftClick(() -> {
                                                           Sounds.click(player);
                                                           List<Potion> potions = this.player.server.registryAccess()
                                                                                                    .registryOrThrow(Registries.POTION)
                                                                                                    .stream().toList();
                                                           Menus.selector(player,
                                                                          Component.translatable("jsst.itemEditor.potionEditor.setPotion"),
                                                                          potions,
                                                                          LabelMaps.POTIONS,
                                                                          result -> {
                                                                              if (result.hasResult())
                                                                                  PotionUtils.setPotion(this.stack, result.result());
                                                                              this.open();
                                                                          });
                                                       })));

        this.effectPaginator.draw();
    }

    private static Component describe(MobEffectInstance instance, float tickrate) {
        MutableComponent description = Component.translatable(instance.getDescriptionId());
        if (instance.getAmplifier() > 0) {
            description = Component.translatable(
                    "potion.withAmplifier",
                    description,
                    Component.translatable("potion.potency." + instance.getAmplifier())
            );
        }

        description = Component.translatable(
                "potion.withDuration",
                description,
                instance.getDuration() < 20 ? instance.getDuration() + " ticks" : MobEffectUtil.formatDuration(instance, 1f, tickrate)
        );

        return description;
    }

    private List<GuiElementInterface> drawPageRow(int index, MobEffectInstance instance) {
        return List.of(
                GuiElementBuilder.from(LabelMaps.MOB_EFFECTS.getLabel(instance.getEffect()))
                                 .setName(describe(instance, this.player.server.tickRateManager().tickrate()))
                                 .addLoreLine(Hints.leftClick(Component.translatable("jsst.itemEditor.potionEditor.setEffect")))
                                 .setCallback(Inputs.leftClick(() -> {
                                     Sounds.click(player);
                                     Menus.selector(player,
                                                    Component.translatable("jsst.itemEditor.potionEditor.setEffect"),
                                                    player.server.registryAccess()
                                                                 .registryOrThrow(Registries.MOB_EFFECT).stream()
                                                                 .toList(),
                                                    LabelMaps.MOB_EFFECTS,
                                                    result -> {
                                                        if (result.hasResult())
                                                            this.effects.set(index, copy(instance, result.result(), null, null));
                                                        this.open();
                                                    });
                                 })).build(),
                GuiElementBuilder.from(Items.CLOCK.getDefaultInstance())
                                 .setName(Component.translatable("jsst.itemEditor.potionEditor.setDuration").setStyle(Styles.INPUT_HINT))
                                 .addLoreLine(Hints.leftClick())
                                 .setCallback(Inputs.leftClick(() -> {
                                     Sounds.click(player);
                                     Menus.duration(player,
                                                    Component.translatable("jsst.itemEditor.potionEditor.setDuration"),
                                                    instance.getDuration(),
                                                    true,
                                                    result -> {
                                                        if (result.hasResult())
                                                            this.effects.set(index, copy(instance, null, result.result(), null));
                                                        this.open();
                                                    });
                                 })).build(),
                GuiElementBuilder.from(Items.GLOWSTONE_DUST.getDefaultInstance())
                                 .setName(Component.translatable("jsst.itemEditor.potionEditor.setAmplifier").setStyle(Styles.INPUT_HINT))
                                 .addLoreLine(Hints.leftClick())
                                 .setCallback(Inputs.leftClick(() -> {
                                     Sounds.click(player);
                                     Menus.integer(player,
                                                   Component.translatable("jsst.itemEditor.potionEditor.setAmplifier"),
                                                   instance.getAmplifier(),
                                                   0,
                                                   127,
                                                   null,
                                                   result -> {
                                                       if (result.hasResult())
                                                           this.effects.set(index, copy(instance, null, null, result.result()));
                                                       this.open();
                                                   });
                                 })).build()
        );
    }
}
