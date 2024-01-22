package red.jackf.jsst.feature.itemeditor.gui;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import red.jackf.jsst.feature.itemeditor.gui.editors.Editor;

import java.util.List;

public record EditorContext(ServerPlayer player, MinecraftServer server, boolean cosmeticOnly, List<Editor.EditorType> allowedEditors) {
}
