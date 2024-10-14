package red.jackf.jsst.impl.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public interface Sounds {
    static void ding(ServerPlayer player) {
        ding(player, 1f);
    }

    static void success(ServerPlayer player) {
        ding(player, 1.2f);
    }

    static void fail(ServerPlayer player) {
        ding(player, 0.8f);
    }

    static void ding(ServerPlayer player, float pitch) {
        playSound(player, SoundEvents.ARROW_HIT_PLAYER, 1f, pitch);
    }

    static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        playSound(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), volume, pitch);
    }

    static void playSound(ServerPlayer player, Holder<SoundEvent> sound, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(sound,
                SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                volume,
                pitch,
                player.getRandom().nextLong()));
    }
}
