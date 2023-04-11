package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.features.Sounds;

import java.util.function.Consumer;

public abstract class Editor {

    protected ItemStack stack;
    protected final ServerPlayer player;
    private final Consumer<ItemStack> completeCallback;
    protected final ItemStack originalStack;

    public Editor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        this.stack = stack;
        this.originalStack = stack.copy();
        this.player = player;
        this.completeCallback = completeCallback;
    }
    /**
     * Should this editor be available for this ItemStack?
     * @param stack ItemStack to check if this editor is applicable for
     * @return If this editor is applicable
     */
    public boolean applies(ItemStack stack) {
        return true;
    }

    /**
     * Create a label for the menu button, made up of an ItemStack.
     * @return The label to show in the main menu
     */
    public abstract ItemStack label();

    public abstract void open();

    protected final void complete() {
        Sounds.success(player);
        this.completeCallback.accept(stack);
    }

    protected final void cancel() {
        Sounds.error(player);
        this.completeCallback.accept(originalStack);
    }

    public interface Supplier {
        Editor get(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback);
    }
}
