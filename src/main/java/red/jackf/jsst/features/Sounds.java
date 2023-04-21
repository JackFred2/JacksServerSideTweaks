package red.jackf.jsst.features;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class Sounds {
    private Sounds() {}

    private static void play(ServerPlayer player, SoundEvent sound, float pitch) {
        player.playNotifySound(sound, SoundSource.PLAYERS, 1f, pitch);
    }

    public static void success(ServerPlayer player) {
       play(player, SoundEvents.NOTE_BLOCK_CHIME.value(), 1.5f);
    }

    public static void complete(ServerPlayer player) {
       play(player, SoundEvents.NOTE_BLOCK_CHIME.value(), 2f);
    }

    public static void error(ServerPlayer player) {
        play(player, SoundEvents.NOTE_BLOCK_CHIME.value(), 0.7f);
    }

    public static void write(ServerPlayer player) {
        play(player, SoundEvents.BOOK_PAGE_TURN, 1f);
    }

    public static void grind(ServerPlayer player) {
        play(player, SoundEvents.GRINDSTONE_USE, 1f);
    }

    public static void interact(ServerPlayer player) {
        interact(player, 1f);
    }

    public static void interact(ServerPlayer player, float pitch) {
        play(player, SoundEvents.NOTE_BLOCK_CHIME.value(), pitch);
    }

    public static void page(ServerPlayer player, int page, int maxPage) {
        interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
    }

    public static void clear(ServerPlayer player) {
        play(player, SoundEvents.BUCKET_EMPTY, 1f);
    }
}
