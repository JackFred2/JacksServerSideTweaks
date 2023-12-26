package red.jackf.jsst.util.sgui;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public interface Sounds {
    float VOLUME = 0.5f;

    static void click(ServerPlayer player) {
        playSound(player, SoundEvents.UI_BUTTON_CLICK, VOLUME, 1f);
    }

    static void close(ServerPlayer player) {
        playSound(player, SoundEvents.UI_BUTTON_CLICK, VOLUME, 0.85f);
    }

    static void scroll(ServerPlayer player, float progress) {
        playSound(player, SoundEvents.UI_BUTTON_CLICK, VOLUME, 0.7f + progress * 0.6f);
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
