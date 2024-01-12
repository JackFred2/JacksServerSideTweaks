package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.mixins.itemeditor.ItemStackAccessor;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.elements.ToggleButton;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoreEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            LoreEditor::new,
            true,
            stack -> true,
            context -> GuiElementBuilder.from(Items.WRITABLE_BOOK.getDefaultInstance())
                                   .setName(Component.translatable("jsst.itemEditor.lore"))
    );

    private final List<Component> lore = new ArrayList<>();

    private final ListPaginator<Component> lorePaginator = ListPaginator.<Component>builder(this)
                                                                        .slots(4, 9, 0, 6)
                                                                        .list(this.lore)
                                                                        .max(30)
                                                                        .modifiable(() -> Component.literal("Lore Line")
                                                                                                   .withStyle(Styles.MINOR_LABEL), true)
                                                                        .onUpdate(this::redraw)
                                                                        .rowDraw(this::getLoreRow)
                                                                        .build();

    public LoreEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.lore"));
        this.loadFromStack();
        this.drawStatic();
    }

    public static List<Component> getLore(ItemStack stack) {
        return GuiElementBuilder.getLore(stack);
    }

    public static void setLore(ItemStack stack, List<Component> lore) {
        if (lore.isEmpty()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);
                display.remove(ItemStack.TAG_LORE);
                if (display.isEmpty()) tag.remove(ItemStack.TAG_DISPLAY);
            }
        } else {
            ListTag loreTag = new ListTag();
            for (Component loreLine : lore) {
                loreTag.add(StringTag.valueOf(Component.Serializer.toJson(loreLine)));
            }

            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag display;
            if (tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                display = tag.getCompound(ItemStack.TAG_DISPLAY);
            } else {
                display = new CompoundTag();
                tag.put(ItemStack.TAG_DISPLAY, display);
            }
            display.put(ItemStack.TAG_LORE, loreTag);
        }
    }

    private List<GuiElementInterface> getLoreRow(int index, Component component) {
        var main = GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                                    .setName(component)
                                    .addLoreLine(Hints.leftClick(Translations.change()))
                                    .setCallback(Inputs.leftClick(() -> {
                                        Sounds.click(player);
                                        EditorMenus.component(player,
                                                              Component.translatable("jsst.itemEditor.lore.line"),
                                                              component,
                                                              newComponent -> {
                                                                  ItemStack copy = this.stack.copy();
                                                                  var lore = getLore(copy);
                                                                  lore.set(index, newComponent);
                                                                  setLore(copy, lore);
                                                                  return copy;
                                                              },
                                                              result -> {
                                                                  if (result.hasResult())
                                                                      this.lore.set(index, result.result());
                                                                  this.open();
                                                              });
                                    })).build();
        return List.of(main);
    }

    private void drawStatic() {
        this.drawPreview(Util.slot(1, 1));

        for (int row = 0; row < 6; row++)
            this.setSlot(Util.slot(3, row), CommonLabels.divider());

        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(this::cancel));

        this.setSlot(Util.slot(0, 4), GuiElementBuilder.from(Items.ENDER_EYE.getDefaultInstance())
                                                       .setName(Component.translatable("jsst.itemEditor.lore.hideTooltipParts"))
                                                       .addLoreLine(Hints.leftClick(Translations.open()))
                                                       .setCallback(Inputs.leftClick(this::openTooltipPartHiding)));
    }

    private void openTooltipPartHiding() {
        Sounds.click(player);
        new TooltipPartHidingGui(player,
                                 this.context,
                                 this.stack,
                                 stack -> {
                                     this.stack = stack;
                                     this.open();
                                 }).open();
    }

    @Override
    protected void reset() {
        super.reset();
        this.loadFromStack();
    }

    private void loadFromStack() {
        this.lore.clear();
        this.lore.addAll(getLore(this.stack));
    }

    @Override
    protected void redraw() {
        setLore(this.stack, this.lore);

        this.drawPreview(Util.slot(1, 1));

        this.lorePaginator.draw();
    }

    private static class TooltipPartHidingGui extends GuiEditor {
        private int mask;

        public TooltipPartHidingGui(
                ServerPlayer player,
                EditorContext context,
                ItemStack initial,
                Consumer<ItemStack> callback) {
            super(MenuType.GENERIC_9x2, player, context, initial, callback, false);
            this.setTitle(Component.translatable("jsst.itemEditor.lore.hideTooltipParts"));
            this.mask = ((ItemStackAccessor) (Object) this.stack).jsst$itemEditor$getTooltipHideMask();

            this.drawStatic();
        }

        private void drawStatic() {
            this.setSlot(Util.slot(7, 0), CommonLabels.divider());
            this.setSlot(Util.slot(7, 1), CommonLabels.divider());

            this.setSlot(Util.slot(8, 1), CommonLabels.cancel(this::cancel));

            this.setSlot(Util.slot(0, 0), this.getPartButton(
                    ItemStack.TooltipPart.ENCHANTMENTS,
                    GuiElementBuilder.from(Items.ENCHANTING_TABLE.getDefaultInstance()).build()
            ));

            this.setSlot(Util.slot(1, 0), this.getPartButton(
                    ItemStack.TooltipPart.MODIFIERS,
                    GuiElementBuilder.from(Items.DIAMOND_SWORD.getDefaultInstance()).hideFlags().build()
            ));

            this.setSlot(Util.slot(2, 0), this.getPartButton(
                    ItemStack.TooltipPart.UNBREAKABLE,
                    GuiElementBuilder.from(Items.STONE_BRICKS.getDefaultInstance()).build()
            ));

            this.setSlot(Util.slot(3, 0), this.getPartButton(
                    ItemStack.TooltipPart.CAN_DESTROY,
                    GuiElementBuilder.from(Items.GOLDEN_PICKAXE.getDefaultInstance()).hideFlags().build()
            ));

            this.setSlot(Util.slot(4, 0), this.getPartButton(
                    ItemStack.TooltipPart.CAN_PLACE,
                    GuiElementBuilder.from(Items.OAK_PLANKS.getDefaultInstance()).build()
            ));

            this.setSlot(Util.slot(5, 0), this.getPartButton(
                    ItemStack.TooltipPart.ADDITIONAL,
                    makeAdditionalIcon()
            ));

            this.setSlot(Util.slot(6, 0), this.getPartButton(
                    ItemStack.TooltipPart.DYE,
                    GuiElementBuilder.from(DyeableLeatherItem.dyeArmor(Items.LEATHER_CHESTPLATE.getDefaultInstance(), List.of((DyeItem) Items.RED_DYE))).hideFlags().build()
            ));

            this.setSlot(Util.slot(0, 1), this.getPartButton(
                    ItemStack.TooltipPart.UPGRADES,
                    GuiElementBuilder.from(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance()).hideFlags().build()
            ));
        }

        private AnimatedGuiElement makeAdditionalIcon() {
            var builder = new AnimatedGuiElementBuilderExt();
            builder.setInterval(16);

            for (var item : List.of(
                    Items.BLUE_BANNER,
                    Items.GLOBE_BANNER_PATTERN,
                    Items.CROSSBOW,
                    Items.ENCHANTED_BOOK,
                    Items.FIREWORK_ROCKET,
                    Items.PAINTING,
                    Items.GOAT_HORN,
                    Items.FILLED_MAP,
                    Items.POTION,
                    Items.MUSIC_DISC_WAIT,
                    Items.WRITTEN_BOOK
            )) {
                builder.setItem(item).hideFlags().saveItemStack();
            }

            return builder.build();
        }

        private ToggleButton getPartButton(ItemStack.TooltipPart tooltipPart, GuiElementInterface icon) {
            return ToggleButton.builder()
                    .label(Component.translatable("jsst.itemEditor.lore.hideTooltipParts." + Util.snakeToCamelCase(tooltipPart.name().toLowerCase())))
                    .makeEnabledGlow()
                    .disabled(icon)
                    .enabled(icon)
                    .initial((this.mask & tooltipPart.getMask()) != 0)
                    .setCallback(newVal -> {
                        Sounds.click(player);
                        if (newVal) {
                            this.mask |= tooltipPart.getMask();
                        } else {
                            this.mask &= ~tooltipPart.getMask();
                        }
                        this.redraw();
                    }).build();
        }

        @Override
        protected void reset() {
            super.reset();
            this.mask = ((ItemStackAccessor) (Object) this.stack).jsst$itemEditor$getTooltipHideMask();
        }

        @Override
        protected void redraw() {
            this.applyMask();
            this.drawPreview(Util.slot(8, 0));
        }

        private void applyMask() {
            if (this.mask == 0) {
                this.stack.removeTagKey("HideFlags");
            } else {
                this.stack.getOrCreateTag().putInt("HideFlags", this.mask);
            }
        }
    }


}
