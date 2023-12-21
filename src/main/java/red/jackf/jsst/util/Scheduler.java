package red.jackf.jsst.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public enum Scheduler {
    INSTANCE;

    private final Map<ServerLevel, Multimap<Long, Consumer<ServerLevel>>> scheduled = new HashMap<>();

    public void setup() {
        ServerTickEvents.START_WORLD_TICK.register(level -> {
            var scheduledForLevel = scheduled.get(level);
            if (scheduledForLevel == null) return;

            for (Consumer<ServerLevel> todo : scheduledForLevel.removeAll(level.getGameTime())) todo.accept(level);
        });

        ServerWorldEvents.UNLOAD.register((server, level) -> scheduled.remove(level));
    }

    public boolean scheduleNextTick(ServerLevel level, Consumer<ServerLevel> toRun) {
        return schedule(level, level.getGameTime() + 1, toRun);
    }

    public boolean schedule(ServerLevel level, Long targetTime, Consumer<ServerLevel> toRun) {
        if (targetTime <= level.getGameTime()) return false;
        Multimap<Long, Consumer<ServerLevel>> scheduledForLevel = scheduled.computeIfAbsent(level, level1 -> MultimapBuilder.treeKeys().linkedListValues().build());
        return scheduledForLevel.put(targetTime, toRun);
    }
}
