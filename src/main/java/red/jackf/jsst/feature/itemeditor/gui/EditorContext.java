package red.jackf.jsst.feature.itemeditor.gui;

import net.minecraft.server.MinecraftServer;

public record EditorContext(MinecraftServer server, boolean cosmeticOnly) {
}
