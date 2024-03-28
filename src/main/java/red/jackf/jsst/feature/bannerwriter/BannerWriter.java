package red.jackf.jsst.feature.bannerwriter;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.Formatting;
import red.jackf.jsst.events.AfterPlacePlaceBlock;
import red.jackf.jsst.feature.Feature;
import red.jackf.jsst.util.Scheduler;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.banners.Banners;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class BannerWriter extends Feature<BannerWriter.Config> {
    public static final BannerWriter INSTANCE = new BannerWriter();
    private static final Logger LOGGER = JSST.getLogger("BannerWriter");

    private final Map<ServerPlayer, Session> sessions = new WeakHashMap<>();

    private BannerWriter() {}

    static Optional<DyeColor> getBaseColourFromBlankBanner(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        if (!(stack.getItem() instanceof BannerItem)) return Optional.empty();
        var patterns = Banners.parseStack(stack);
        if (!patterns.patterns().isEmpty()) return Optional.empty();
        return Optional.ofNullable(patterns.baseColour());
    }

    @Override
    public void setup() {
        Characters.INSTANCE.setup();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Map.Entry<ServerPlayer, Session> entry : List.copyOf(this.sessions.entrySet())) {
                ServerPlayer player = entry.getKey();
                Session session = entry.getValue();
                if (session.isEmpty()) {
                    this.sessions.remove(player);
                    player.sendSystemMessage(Formatting.successLine(Component.translatable("jsst.bannerWriter.complete")));
                    Sounds.ding(player, 1.2f);
                } else if (!session.stillValid(player)) {
                    terminateSession(player);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.sessions.clear());

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> this.sessions.remove(handler.getPlayer()));

        AfterPlacePlaceBlock.EVENT.register((player, level, pos, placedState, blockStack) -> {
            if (!this.sessions.containsKey(player)) return;
            Session session = this.sessions.get(player);
            if (!session.isCorrectBanner(blockStack)) return;
            Optional<ItemStack> letterOptional = session.next();
            if (letterOptional.isEmpty()) {
                terminateSession(player);
                return;
            }
            final ItemStack letter = letterOptional.get();
            final Banners.BannerPatterns banner = Banners.parseStack(letter);
            if (banner.baseColour() != session.getBackgroundColour()) {
                BlockState replacement;
                if (placedState.getBlock() instanceof BannerBlock) {
                    replacement = BannerBlocks.FLOOR.get(banner.baseColour()).defaultBlockState()
                            .setValue(BannerBlock.ROTATION, placedState.getValue(BannerBlock.ROTATION));
                } else {
                    replacement = BannerBlocks.WALL.get(banner.baseColour()).defaultBlockState()
                            .setValue(WallBannerBlock.FACING, placedState.getValue(WallBannerBlock.FACING));
                }
                level.setBlock(pos, replacement, Block.UPDATE_ALL);
                Scheduler.INSTANCE.scheduleNextTick(level, serverLevel -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof BannerBlockEntity bbe)
                        bbe.fromItem(letter);
                    for (ServerPlayer lookingPlayer : PlayerLookup.tracking(be))
                        lookingPlayer.connection.send(be.getUpdatePacket());
                });
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BannerBlockEntity bbe)
                    bbe.fromItem(letter);
            }

            Sounds.ding(player, 1f);
        });
    }

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().bannerWriter;
    }

    public void start(ServerPlayer player, DyeColor backgroundColour, DyeColor textColour, String text) {
        LOGGER.debug("Starting banner writer for {} (fg:{}, bg:{}, text:{})", player.getGameProfile().getName(), textColour, backgroundColour, text);
        this.sessions.put(player, new Session(backgroundColour, textColour, text));
        player.sendSystemMessage(Formatting.successLine(Component.translatable("jsst.bannerWriter.start",
                Component.literal(text).withStyle(Styles.EXAMPLE),
                Component.literal(textColour.getName()).withStyle(Styles.EXAMPLE),
                Component.literal(backgroundColour.getName()).withStyle(Styles.EXAMPLE)
        )));
        Sounds.ding(player, 1f);
    }

    // errored or cancelled
    public void terminateSession(ServerPlayer player) {
        this.sessions.remove(player);
        player.sendSystemMessage(Formatting.errorLine(Component.translatable("jsst.bannerWriter.cancelled")));
        Sounds.ding(player, 0.8f);
    }

    public static class Config extends Feature.Config {
        @Comment("""
                Whether the banner writer command should be available only to operators, or to anyone.
                TODO: Add permissions node
                Options: true, false
                Default: false""")
        public boolean operatorOnly = false;

        @Comment("""
                Adds a dedicated /bannerWriter command to use the banner writer as an alternative to /jsst bannerWriter.
                Requires a server restart or world reload to take effect.
                Options: true, false
                Default: true""")
        public boolean dedicatedCommand = true;
    }
}
