package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;

import java.util.function.Consumer;

/**
 * Represents a complex action to be done to an item stack.
 */
public abstract class GuiEditor extends SimpleGui implements Editor {
    protected final EditorContext context;
    private final ItemStack initial;
    private final Consumer<ItemStack> callback;

    protected ItemStack stack;

    public GuiEditor(
            MenuType<?> type,
            ServerPlayer player,
            EditorContext context,
            ItemStack initial,
            Consumer<ItemStack> callback,
            boolean usePlayerSlots) {
        super(type, player, usePlayerSlots);
        this.context = context;
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
        ItemStack result = getResult();
        this.setSlot(slot, JSSTElementBuilder.from(result)
                        .leftClick(Translations.save(), this::complete)
                        .rightClick(Translations.reset(), this::reset));
    }

    private void reset() {
        Sounds.clear(player);
        this.stack = initial.copy();
        this.onReset();
        this.redraw();
    }

    protected void onReset() {}

    protected ItemStack getResult() {
        return this.stack.copy();
    }

    protected final void complete() {
        Sounds.click(player);
        this.callback.accept(getResult());
    }

    protected final void cancel() {
        Sounds.close(player);
        this.callback.accept(this.initial);
    }
}
