package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.sgui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoreEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            LoreEditor::new,
            true,
            stack -> true,
            () -> GuiElementBuilder.from(Items.WRITABLE_BOOK.getDefaultInstance())
                                   .setName(Component.translatable("jsst.itemEditor.lore"))
    );

    private final List<Component> lore = new ArrayList<>();

    private final ListPaginator<Component> lorePaginator = ListPaginator.<Component>builder(this)
                                                                        .at(4, 9, 0, 6)
                                                                        .list(this.lore)
                                                                        .max(30)
                                                                        .modifiable(() -> Component.literal("Lore Line").withStyle(Styles.MINOR_LABEL), true)
                                                                        .onUpdate(this::redraw)
                                                                        .rowDraw(this::getLoreRow)
                                                                        .build();

    public LoreEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x6, player, context, initial, callback);
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


}
