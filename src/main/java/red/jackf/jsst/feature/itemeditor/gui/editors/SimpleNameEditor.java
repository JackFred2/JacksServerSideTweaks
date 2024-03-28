package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.function.Consumer;

public class SimpleNameEditor extends GuiEditor {
    public static final EditorType TYPE = new EditorType(
            JSST.id("simple_name"),
            SimpleNameEditor::new,
            true,
            false,
            ignored -> true,
            SimpleNameEditor::getLabel
    );

    public SimpleNameEditor(
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(MenuType.GENERIC_9x1, player, context, initial, callback, false);
        this.setTitle(Component.translatable("jsst.itemEditor.simpleName"));
    }

    public static GuiElementBuilderInterface<?> getLabel(EditorContext context) {
        return JSSTElementBuilder.from(Items.PAPER).setName(Component.translatable("jsst.itemEditor.simpleName"));
    }

    @Override
    protected void redraw() {
        this.drawPreview(0);

        this.setSlot(1, CommonLabels.divider());

        this.setSlot(2, JSSTElementBuilder.ui(Items.WRITABLE_BOOK)
                .leftClick(Component.translatable("jsst.itemEditor.simpleName.changeText"), this::changeText));

        this.setSlot(3, JSSTElementBuilder.ui(Items.GLOWSTONE)
                .leftClick(Component.translatable("jsst.itemEditor.simpleName.changeStyle"), this::changeStyle));

        if (this.stack.hasCustomHoverName()) {
            this.setSlot(7, JSSTElementBuilder.ui(Items.GRINDSTONE)
                    .leftClick(Translations.clear(), this::clearName));
        } else {
            this.clearSlot(7);
        }
        this.setSlot(8, CommonLabels.cancel(this::cancel));
    }

    private void changeStyle() {
        Sounds.click(player);
        EditorMenus.componentStyle(player,
                this.stack.getHoverName(),
                opt -> {
                    opt.ifPresent(this.stack::setHoverName);
                    this.open();
                });
    }

    private void changeText() {
        Sounds.click(player);
        Menus.stringBuilder(player)
                .title(Component.translatable("jsst.itemEditor.simpleName.changeText"))
                .initial(stack.getHoverName().getString())
                .createAndShow(result -> {
                    if (result.hasResult())
                        this.stack.setHoverName(Component.literal(result.result())
                                .setStyle(this.stack.getHoverName().getStyle()));
                    this.open();
                });
    }

    private void clearName() {
        Sounds.grind(player);
        this.stack.resetHoverName();
        this.redraw();
    }
}
