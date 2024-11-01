package red.jackf.jsst.impl.feature.mapeditor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import red.jackf.jsst.impl.utils.Sounds;

public interface MapSounds {
    static void scribble(ServerPlayer player) {
        Sounds.playSound(player, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1.1f);
    }

    static void page(ServerPlayer player) {
        Sounds.playSound(player, SoundEvents.BOOK_PAGE_TURN, 1f);
    }

    static void erase(ServerPlayer player) {
        Sounds.playSound(player, SoundEvents.BOOK_PUT, 1f);
    }
}
