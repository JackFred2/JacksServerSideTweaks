package red.jackf.jsst.impl.feature.itemeditor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditSession {
    private final ServerPlayer player;
    private final ItemStack initial;
    private final Supplier<Boolean> stillValid;
    private final Consumer<ItemStack> onComplete;
    private ItemStack stack;

    public EditSession(ServerPlayer player,
                       ItemStack initial,
                       Supplier<Boolean> stillValid,
                       Consumer<ItemStack> onComplete) {
        this.player = player;
        this.initial = initial;
        this.stillValid = stillValid;
        this.onComplete = onComplete;

        this.setStack(this.getInitial());
    }

    public ItemStack getInitial() {
        return this.initial.copy();
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public boolean stillValid() {
        return !this.player.isRemoved() && this.stillValid.get();
    }

    public void cancel() {
        this.player.closeContainer();
    }
}
