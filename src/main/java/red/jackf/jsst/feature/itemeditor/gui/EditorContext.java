package red.jackf.jsst.feature.itemeditor.gui;

import net.minecraft.server.MinecraftServer;
import red.jackf.jsst.feature.itemeditor.gui.editors.Editor;

import java.util.List;

public record EditorContext(MinecraftServer server, boolean cosmeticOnly, List<Editor.EditorType> allowedEditors) {
}
