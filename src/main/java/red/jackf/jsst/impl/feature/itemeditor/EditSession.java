package red.jackf.jsst.impl.feature.itemeditor;

import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.itemeditor.gui.editors.Editor;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EditSession {
    private final ServerPlayer player;
    private final ItemStack initial;
    private final boolean cosmeticOnly;
    private final Supplier<Boolean> stillValid;
    private boolean hasEnded = false;
    private ItemStack stack;

    private boolean showDeveloperTools = false;

    protected EditSession(ServerPlayer player,
                       ItemStack initial,
                       boolean cosmeticOnly,
                       Supplier<Boolean> stillValid) {
        this.player = player;
        this.initial = initial;
        this.cosmeticOnly = cosmeticOnly;
        this.stillValid = stillValid;

        this.setStack(this.getInitial());
    }

    public Stream<Editor.Type<?>> streamEditors() {
        Set<ResourceLocation> disabled = JSSTConfig.INSTANCE.instance().itemEditor.disabledEditors;

        return ItemEditor.EDITORS.stream()
                .filter(type -> !disabled.contains(type.getId()))
                .filter(type -> type.supportsCosmetic() || !cosmeticOnly);
    }

    public boolean isShowingDeveloperTools() {
        return this.showDeveloperTools;
    }

    public void setShowingDeveloperTools(boolean showDeveloperTools) {
        this.showDeveloperTools = showDeveloperTools;
    }

    public boolean isCosmeticOnly() {
        return this.cosmeticOnly;
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

    public RegistryAccess registries() {
        return this.player.serverLevel().registryAccess();
    }

    public boolean stillValid() {
        return !hasEnded && !this.player.isRemoved() && this.stillValid.get();
    }

    public void end() {
        if (this.player.containerMenu instanceof VirtualScreenHandlerInterface virtual && virtual.getGui() instanceof SimpleGuiExt gui) {
            gui.close();
        }
        this.hasEnded = true;
    }
}
