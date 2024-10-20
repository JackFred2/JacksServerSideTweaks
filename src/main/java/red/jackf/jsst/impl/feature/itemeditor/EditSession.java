package red.jackf.jsst.impl.feature.itemeditor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class EditSession {
    private final ServerPlayer player;
    private final ItemStack initial;
    private final Supplier<Boolean> stillValid;
    private boolean hasEnded = false;
    private ItemStack stack;

    protected EditSession(ServerPlayer player,
                       ItemStack initial,
                       Supplier<Boolean> stillValid) {
        this.player = player;
        this.initial = initial;
        this.stillValid = stillValid;

        this.setStack(this.getInitial());
    }

    public ItemStack getInitial() {
        return this.initial.copy();
    }

    public ItemStack getStack() {
        return this.stack.copy();
    }

    @ApiStatus.Internal
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public boolean stillValid() {
        return !hasEnded && !this.player.isRemoved() && this.stillValid.get();
    }

    public void end() {
        this.player.closeContainer();
        this.hasEnded = true;
    }
}
