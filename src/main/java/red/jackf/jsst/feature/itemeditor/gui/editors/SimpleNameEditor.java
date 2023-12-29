package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.menus.StyleMenu;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.function.Consumer;

public class SimpleNameEditor extends GuiEditor {
    public SimpleNameEditor(
            ServerPlayer player,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x1, player, initial, callback);
        this.setTitle(Component.translatable("jsst.itemEditor.simpleName"));
    }

    @Override
    public GuiElementBuilder getLabel() {
        return GuiElementBuilder.from(new ItemStack(Items.PAPER))
                                .setName(Component.translatable("jsst.itemEditor.simpleName"));
    }

    @Override
    protected void redraw() {
        this.drawPreview(0);

        this.setSlot(1, CommonLabels.divider());

        this.setSlot(2, GuiElementBuilder.from(new ItemStack(Items.WRITABLE_BOOK))
                .setName(Component.translatable("jsst.itemEditor.simpleName.changeText").withStyle(Styles.INPUT_HINT))
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(this::changeText)));

        this.setSlot(3, GuiElementBuilder.from(new ItemStack(Items.GLOWSTONE))
                .setName(Component.translatable("jsst.itemEditor.simpleName.changeStyle").withStyle(Styles.INPUT_HINT))
                .addLoreLine(Hints.leftClick())
                .setCallback(Inputs.leftClick(this::changeStyle)));

        if (this.stack.hasCustomHoverName()) {
            this.setSlot(7, GuiElementBuilder.from(new ItemStack(Items.GRINDSTONE))
                                             .setName(Translations.clear().withStyle(Styles.INPUT_HINT))
                                             .addLoreLine(Hints.leftClick())
                                             .setCallback(Inputs.leftClick(this::clearName)));
        } else {
            this.clearSlot(7);
        }
        this.setSlot(8, CommonLabels.cancel(this::cancel));
    }

    private void changeStyle() {
        Sounds.click(player);
        new StyleMenu(player,
                      this.stack.getHoverName(),
                      Component.translatable("jsst.itemEditor.simpleName.changeStyle"),
                          opt -> {
                              opt.ifPresent(c -> this.stack.setHoverName(c));
                              this.open();
                          }).open();
    }

    private void changeText() {
        Sounds.click(player);
        Menus.stringBuilder(player)
             .title(Component.translatable("jsst.itemEditor.simpleName.changeText"))
             .initial(stack.getHoverName().getString())
             .createAndShow(opt -> {
                 opt.ifPresent(s -> this.stack.setHoverName(Component.literal(s).setStyle(this.stack.getHoverName().getStyle())));
                 this.open();
             });
    }

    private void clearName() {
        Sounds.grind(player);
        this.stack.resetHoverName();
        this.redraw();
    }
}
