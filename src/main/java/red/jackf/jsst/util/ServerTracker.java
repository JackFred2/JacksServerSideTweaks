package red.jackf.jsst.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerTracker {
    private static @Nullable MinecraftServer server = null;

    public static void setup() {
        ServerLifecycleEvents.SERVER_STARTED.register(server1 -> server = server1);

        ServerLifecycleEvents.SERVER_STOPPED.register(server1 -> server = server1);
    }

    public static List<ServerLevel> eachLoadedLevel() {
        if (server == null) return Collections.emptyList();
        List<ServerLevel> levels = new ArrayList<>();
        server.getAllLevels().forEach(levels::add);
        return levels;
    }

    public static @Nullable MinecraftServer getServer() {
        return server;
    }
}
