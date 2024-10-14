package red.jackf.jsst.impl.feature.bannerwriter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.JSST;

import java.util.Optional;

final class Session {
    private final ServerPlayer player;
    private final DyeColor backgroundColour;
    private final DyeColor textColour;
    private final String text;
    @Nullable
    private CustomBossEvent bossBar = null;
    private int charactersLeft;

    Session(ServerPlayer player, DyeColor backgroundColour, DyeColor textColour, String text) {
        this.player = player;
        this.backgroundColour = backgroundColour;
        this.textColour = textColour;
        this.text = text;

        this.charactersLeft = text.length();
    }

    public DyeColor getBackgroundColour() {
        return backgroundColour;
    }

    public DyeColor getTextColour() {
        return textColour;
    }

    private Component formatText() {
        return Component.translatable("jsst.bannerWriter.bossbar",
                Component.empty()
                         .append(Component.literal(text.substring(0, text.length() - charactersLeft)).withStyle(ChatFormatting.GREEN))
                         .append(Component.literal(text.substring(text.length() - charactersLeft)).withStyle(ChatFormatting.RED))
        );
    }

    public boolean hasFinished() {
        return charactersLeft == 0;
    }

    public boolean stillValid() {
        if (player.isRemoved()) return false;
        if (text.isEmpty()) return false;
        if (charactersLeft <= 0) return false;

        Optional<DyeColor> handColour = BannerWriter.getHeldBannerColour(player.getItemInHand(InteractionHand.MAIN_HAND));
        if (handColour.isEmpty() || handColour.get() != this.backgroundColour) return false;

        return true;
    }

    public Optional<Character> nextChar() {
        if (charactersLeft <= 0) {
            return Optional.empty();
        } else {
            return Optional.of(text.charAt(text.length() - (charactersLeft--)));
        }
    }

    public void start() {
        MinecraftServer server = player.getServer();
        //noinspection DataFlowIssue
        CustomBossEvents events = server.getCustomBossEvents();

        if (bossBar != null) {
            events.remove(bossBar);
        }

        player.sendSystemMessage(Component.translatable("jsst.bannerWriter.started", text));
        ResourceLocation id = JSST.id("bannerwriter_%s".formatted(Long.toHexString(player.getRandom().nextLong())));

        bossBar = events.create(id, formatText());
        bossBar.setColor(BossEvent.BossBarColor.GREEN);
        bossBar.addPlayer(player);
        bossBar.setMax(text.length());
    }

    public void refresh() {
        if (bossBar != null) {
            bossBar.setName(formatText());
            bossBar.setValue(text.length() - charactersLeft);
        }
    }

    public void terminate() {
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            //noinspection DataFlowIssue
            player.getServer().getCustomBossEvents().remove(bossBar);
        }
    }
}
