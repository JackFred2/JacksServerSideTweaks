package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import red.jackf.jsst.util.sgui.Hints;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;

import java.util.function.Consumer;

/**
 * Represents a complex action to be done to an item stack. Be aware that {@link GuiElementBuilder#from(ItemStack)} tampers
 * with the passed stack, make sure to use {@link ItemStack#copy()}
 */
public abstract class GuiEditor extends SimpleGui implements Editor {
    private final boolean cosmeticOnly;
    private final ItemStack initial;
    private final Consumer<ItemStack> callback;

    protected ItemStack stack;

    public GuiEditor(
            MenuType<?> type,
            ServerPlayer player,
            boolean cosmeticOnly,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        super(type, player, false);
        this.cosmeticOnly = cosmeticOnly;
        this.initial = initial;
        this.callback = callback;
        this.stack = initial.copy();
    }

    protected abstract void redraw();

    @Override
    @MustBeInvokedByOverriders
    public void onOpen() {
        this.redraw();
    }

    @Override
    public void onClose() {
        this.callback.accept(this.initial);
    }

    @Override
    @MustBeInvokedByOverriders
    public void run() {
        Sounds.click(player);
        this.open();
    }

    protected ItemStack getInitial() {
        return this.initial.copy();
    }

    protected final void drawPreview(int slot) {
        this.setSlot(slot, GuiElementBuilder.from(this.stack.copy())
                .setName(Util.getLabelAsTooltip(this.stack))
                .addLoreLine(Hints.leftClick(Translations.save()))
                .addLoreLine(Hints.rightClick(Translations.reset()))
                .setCallback(this::clickPreview));
    }

    private void clickPreview(ClickType type) {
        if (type == ClickType.MOUSE_LEFT) {
            this.complete();
        } else if (type == ClickType.MOUSE_RIGHT) {
            Sounds.clear(player);
            this.reset();
            this.redraw();
        }
    }

    protected void reset() {
        this.stack = initial.copy();
    }

    protected void complete() {
        Sounds.click(player);
        this.callback.accept(this.stack);
    }

    protected void cancel() {
        Sounds.close(player);
        this.callback.accept(this.initial);
    }
}
