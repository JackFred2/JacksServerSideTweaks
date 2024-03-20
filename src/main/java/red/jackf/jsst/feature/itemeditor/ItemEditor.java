package red.jackf.jsst.feature.itemeditor;

import blue.endless.jankson.Comment;
import blue.endless.jankson.annotation.Nullable;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.JSSTCommand;
import red.jackf.jsst.feature.Feature;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.feature.itemeditor.gui.ItemEditorGui;
import red.jackf.jsst.feature.itemeditor.gui.editors.*;
import red.jackf.jsst.feature.itemeditor.previouscolours.EditorColourHistory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemEditor extends Feature<ItemEditor.Config> {
    public static final ItemEditor INSTANCE = new ItemEditor();
    private ItemEditor() {}

    private static final List<Editor.EditorType> EDITORS = List.of(
            SimpleNameEditor.TYPE,
            NameEditor.TYPE,
            LoreEditor.TYPE,
            DurabilityEditor.TYPE,
            TrimEditor.TYPE,
            EnchantmentEditor.TYPE,
            BannerEditor.TYPE,
            PotionEditor.TYPE,
            SuspiciousStewEditor.TYPE,
            WrittenBookEditor.TYPE,
            PlayerHeadEditor.TYPE,
            CustomModelDataEditor.TYPE,
            StackNBTPrinter.TYPE
    );

    public static final Logger LOGGER = JSST.getLogger("Item Editor");

    @Override
    public void setup() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ((EditorColourHistory) newPlayer).jsst$itemEditor$copyFrom(oldPlayer));
    }

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
        EditorAccess access = getAccessForPlayer(player);
        if (access == EditorAccess.NONE) return; // shouldn't happen, but just in case
        LOGGER.debug("Starting Item Editor for {}, base item {}", player.getName().getString(), handItem);

        List<Editor.EditorType> editors = EDITORS.stream()
                .filter(type -> !config().disabledEditors.contains(type.id()))
                .filter(type -> access == EditorAccess.FULL || type.cosmeticOnly())
                .toList();

        EditorContext context = new EditorContext(player, player.server, access == EditorAccess.COSMETIC, editors);

        new ItemEditorGui(player,
                handItem,
                returnSlot,
                context).open();
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
                Whether to enable the Planet Minecraft import / export button in the Banner Editor.
                Options: true, false
                Default: true""")
        public boolean planetMinecraftButton = true;

        @Comment("""
                Adds a dedicated /itemEditor command to open the item editor as an alternative to /jsst itemEditor.
                Requires a server restart or world reload to take effect.
                Options: true, false
                Default: false""")
        public boolean dedicatedCommand = false;

        @Comment("""
                A list of globally disabled editors. The IDs for editors are shown when 'developer mode' is enabled in the GUI.
                Options: A set of editor IDs, in the form mod:editor
                Default: No disabled editors""")
        public Set<ResourceLocation> disabledEditors = new HashSet<>();
    }
}
