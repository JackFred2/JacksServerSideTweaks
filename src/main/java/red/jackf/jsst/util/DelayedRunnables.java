package red.jackf.jsst.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple setup to delay runnables to a certain tick per level.
 */
public class DelayedRunnables {
    private static boolean isSetup = false;

    private static final Map<ServerLevel, Multimap<Long, Runnable>> delayedRuns = new HashMap<>();

    public static void setup() {
        if (isSetup) throw new AssertionError("Attempted to setup() twice!");
        isSetup = true;

        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (delayedRuns.containsKey(level)) {
                var levelMap = delayedRuns.get(level);
                levelMap.get(level.getGameTime()).forEach(Runnable::run);
                levelMap.removeAll(level.getGameTime());
            }
        });
    }

    public static void schedule(ServerLevel level, Long target, Runnable runnable) {
        delayedRuns.computeIfAbsent(level, l -> HashMultimap.create()).put(target, runnable);
    }
}
