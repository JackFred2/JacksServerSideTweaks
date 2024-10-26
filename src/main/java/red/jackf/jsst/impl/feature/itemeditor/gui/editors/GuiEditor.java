package red.jackf.jsst.impl.feature.itemeditor.gui.editors;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import red.jackf.jsst.impl.feature.itemeditor.EditSession;
import red.jackf.jsst.impl.feature.itemeditor.ItemEditor;
import red.jackf.jsst.impl.feature.itemeditor.Result;
import red.jackf.jsst.impl.utils.Callbacks;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

import java.util.function.Consumer;

public abstract class GuiEditor extends SimpleGuiExt implements Editor {
    protected final EditSession session;
    private final Consumer<Result> resultConsumer;
    protected ItemStack stack;

    public GuiEditor(EditSession session, Consumer<Result> resultConsumer, Component title, MenuType<?> type, boolean manipulatePlayerSlots) {
        super(type, session.getPlayer(), manipulatePlayerSlots);
        this.resultConsumer = Callbacks.singleUse(resultConsumer);
        this.session = session;
        this.stack = session.getStack();
        this.drawStatic();
        this.setTitle(title);
        ItemEditor.LOGGER.info("Init for {}", this.getClass().getSimpleName());
    }

    @Override
    @MustBeInvokedByOverriders
    public void start() {
        Sounds.UI.click(player);
        this.open();
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    protected void complete() {
        Sounds.UI.click(this.session.getPlayer());
        this.resultConsumer.accept(Result.of(this.stack));
    }

    protected void cancel() {
        Sounds.UI.close(this.session.getPlayer());
        this.resultConsumer.accept(Result.empty());
    }

    @MustBeInvokedByOverriders
    protected void reset() {
        Sounds.UI.reset(this.session.getPlayer());
        this.stack = this.session.getStack();
        this.refresh();
    }

    protected <T> Registry<T> lookupRegistry(ResourceKey<Registry<T>> key) {
        return RegistryUtils.lookup(this.session.getPlayer().serverLevel().registryAccess(), key);
    }

    // previews

    /**
     * Draws the current stack at a given column and row
     */
    protected void drawPreview(int column, int row) {
        this.drawPreview(this.getSlotFor(column, row));
    }

    /**
     * Draws the current stack at a given slot
     */
    protected void drawPreview(int slot) {
        this.setSlot(slot, JSSTElementBuilder.from(this.stack.copy())
                .leftClick(Translations.save(), this::complete)
                .rightClick(Translations.reset(), this::reset));
    }
}
