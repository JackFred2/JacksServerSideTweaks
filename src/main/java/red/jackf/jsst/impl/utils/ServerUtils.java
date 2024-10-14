package red.jackf.jsst.impl.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerUtils {
    static void refreshCommands(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }
}
