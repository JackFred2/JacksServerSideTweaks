package red.jackf.jsst.feature.itemeditor;

import blue.endless.jankson.Comment;
import blue.endless.jankson.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.JSSTCommand;
import red.jackf.jsst.feature.Feature;
import red.jackf.jsst.feature.itemeditor.gui.ItemEditorGui;

public class ItemEditor extends Feature<ItemEditor.Config> {
    public static final ItemEditor INSTANCE = new ItemEditor();
    private ItemEditor() {}

    private static final Logger LOGGER = JSST.getLogger("Item Editor");

    @Override
    public void setup() {}

    @Override
    public void reload(Config current) {
        super.reload(current);
        JSSTCommand.resendCommands();
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().itemEditor;
    }

    public void newSession(ServerPlayer player, ItemStack handItem, @Nullable EquipmentSlot returnSlot) {
        var access = getAccessForPlayer(player);
        if (access == EditorAccess.NONE) return;
        LOGGER.debug("Starting Item Editor for {}, base item {}", player.getName().getString(), handItem);
        new ItemEditorGui(player, handItem, returnSlot, access == EditorAccess.COSMETIC).open();
    }

    public EditorAccess getAccessForPlayer(@Nullable ServerPlayer player) {
        if (player == null) return EditorAccess.NONE;
        if (player.hasPermissions(4)) {
            return EditorAccess.FULL;
        } else if (config().cosmeticOnlyModeAvailable) {
            return EditorAccess.COSMETIC;
        } else {
            return EditorAccess.NONE;
        }
    }

    public enum EditorAccess {
        NONE,
        COSMETIC,
        FULL
    }

    public static class Config extends Feature.Config {
        @Comment("""
                Allows non-operator players to use the item editor with only cosmetic editors available (name, lore, colour, etc).
                Note that there are some minimal changes (renaming not needing 1 XP level, for example).
                Options: true, false
                Default: false""")
        public boolean cosmeticOnlyModeAvailable = false;

        @Comment("""
                Adds a dedicated /itemEditor command to open the item editor as an alternative to /jsst itemEditor.
                Requires a server restart or world reload to take effect.
                Options: true, false
                Default: false""")
        public boolean dedicatedCommand = false;
    }
}
