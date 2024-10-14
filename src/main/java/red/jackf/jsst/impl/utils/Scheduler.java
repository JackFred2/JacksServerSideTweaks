package red.jackf.jsst.impl.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Consumer;

public class Scheduler {
    private static final Map<ServerLevel, Multimap<Long, Consumer<ServerLevel>>> SCHEDULED = new HashMap<>();

    public static void setup() {
        ServerWorldEvents.UNLOAD.register((server, level) -> {
            SCHEDULED.remove(level);
        });

        ServerTickEvents.END_WORLD_TICK.register(level -> {
            Multimap<Long, Consumer<ServerLevel>> map = SCHEDULED.get(level);
            if (map == null) return;

            long time = level.getGameTime();
            SortedMap<Long, Collection<Consumer<ServerLevel>>> toRun = ((SortedMap<Long, Collection<Consumer<ServerLevel>>>) map.asMap())
                    .headMap(time + 1);

            for (Collection<Consumer<ServerLevel>> perTickConsumers : toRun.values()) {
                for (Consumer<ServerLevel> consumer : perTickConsumers) {
                    consumer.accept(level);
                }
            }

            toRun.clear();
        });
    }

    public static boolean schedule(ServerLevel level, long targetTime, Consumer<ServerLevel> consumer) {
        if (targetTime <= level.getGameTime()) return false;

        SCHEDULED.computeIfAbsent(level, Scheduler::newMap).put(targetTime, consumer);

        return true;
    }

    private static Multimap<Long, Consumer<ServerLevel>> newMap(ServerLevel level) {
        return MultimapBuilder.treeKeys()
                .linkedListValues()
                .build();
    }
}
