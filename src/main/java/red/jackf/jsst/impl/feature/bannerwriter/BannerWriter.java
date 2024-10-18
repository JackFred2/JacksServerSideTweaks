package red.jackf.jsst.impl.feature.bannerwriter;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
//? if >=1.21.1
import net.minecraft.core.component.DataComponents;
//? if <1.21.1
/*import net.minecraft.nbt.CompoundTag;*/
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BannerItem;
//? if <1.21.1
/*import net.minecraft.world.item.BlockItem;*/
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
//? if >=1.21.1
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.Banners;
import red.jackf.jsst.impl.utils.Scheduler;
import red.jackf.jsst.impl.utils.Sounds;

import java.util.*;

public class BannerWriter {
    private static final Map<ServerPlayer, Session> CURRENT_SESSIONS = new HashMap<>();

    public static void setup() {
        // cleanup old just in case
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (ResourceLocation id : server.getCustomBossEvents().getIds()) {
                if (id.getNamespace().equals(JSST.MOD_ID) && id.getPath().startsWith("bannerwriter_")) {
                    server.getCustomBossEvents().get(id);
                }
            }

            Alphabet.INSTANCE.reload(server.registryAccess());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Set<ServerPlayer> toRemove = new HashSet<>();

            for (Map.Entry<ServerPlayer, Session> pair : CURRENT_SESSIONS.entrySet()) {
                if (!pair.getValue().stillValid()) {
                    toRemove.add(pair.getKey());
                }
            }

            toRemove.forEach(player -> {
                removeAndTerminate(player);
                Sounds.fail(player);
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("bannerWriter");
            root.requires(stack -> stack.isPlayer()
                    && JSSTConfig.INSTANCE.instance().bannerWriter.enabled
                    && stack.hasPermission(JSSTConfig.INSTANCE.instance().bannerWriter.permissionlevel));

            var argument = Commands.argument("text", StringArgumentType.greedyString());
            argument.executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                String text = StringArgumentType.getString(context, "text").strip().toLowerCase(Locale.ROOT);

                if (!JSSTConfig.INSTANCE.instance().bannerWriter.enabled) {
                    player.sendSystemMessage(Component.translatable("jsst.feature.disabled"));
                    return 0;
                }

                // check if empty somehow
                if (text.isBlank()) {
                    player.sendSystemMessage(Component.translatable("jsst.bannerWriter.noText"));
                    return 0;
                }

                // remove characters not supported by the current alphabet
                Set<Character> invalid = new LinkedHashSet<>();

                for (char c : text.toCharArray()) {
                    if (!Alphabet.INSTANCE.containsDesignFor(c)) {
                        invalid.add(c);
                    }
                }

                if (!invalid.isEmpty()) {
                    StringBuilder built = new StringBuilder();
                    invalid.forEach(built::append);
                    player.sendSystemMessage(Component.translatable("jsst.bannerWriter.unsupportedCharacters", built.toString()));
                    return 0;
                }

                // check if banner is valid (no patterns)

                ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);

                Optional<DyeColor> bannerColour = getHeldBannerColour(handStack);

                //? if >=1.21.1 {
                if (bannerColour.isEmpty() || !handStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty()) {
                //?} else {
                /*CompoundTag beData = BlockItem.getBlockEntityData(handStack);
                if (bannerColour.isEmpty() || (beData != null && beData.contains("Patterns"))) {
                *///?}
                    player.sendSystemMessage(Component.translatable("jsst.bannerWriter.invalidBanner"));
                    return 0;
                }

                // valid to start
                removeAndTerminate(player);

                Session session = new Session(player, bannerColour.get(), getTextColourForBackground(bannerColour.get()), text);
                session.start();
                CURRENT_SESSIONS.put(player, session);

                return text.length();
            });

            root.then(argument);

            dispatcher.register(root);
        });
    }

    private static void removeAndTerminate(ServerPlayer player) {
        Session old = CURRENT_SESSIONS.remove(player);
        if (old != null) {
            old.terminate();
        }
    }

    static Optional<DyeColor> getHeldBannerColour(ItemStack stack) {
        if (stack.getItem() instanceof BannerItem bannerItem) {
            return Optional.of(bannerItem.getColor());
        } else {
            return Optional.empty();
        }
    }

    private static DyeColor getTextColourForBackground(DyeColor background) {
        return background == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE;
    }

    public static void onPlayerPlace(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack stack) {
        if (!CURRENT_SESSIONS.containsKey(player)) return;

        Session session = CURRENT_SESSIONS.get(player);
        if (getHeldBannerColour(stack).map(stackColour -> stackColour != session.getBackgroundColour()).orElse(true)) return;

        if (!session.stillValid()) return;

        var next = session.nextChar();
        if (next.isEmpty()) return;

        ItemStack letter = Alphabet.INSTANCE.create(next.get(), session.getBackgroundColour(), session.getTextColour());
        DyeColor letterColour = getHeldBannerColour(letter).orElse(session.getBackgroundColour());

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractBannerBlock bannerBlock)) return;

        boolean refreshData;

        if (bannerBlock.getColor() != letterColour) {
            BlockState replacement;
            if (state.getBlock() instanceof BannerBlock) {
                replacement = Banners.ByColour.FLOOR.get(letterColour).withPropertiesOf(state);
            } else {
                replacement = Banners.ByColour.WALL.get(letterColour).withPropertiesOf(state);
            }

            level.setBlock(pos, replacement, BannerBlock.UPDATE_ALL);
            refreshData = true;
        } else {
            refreshData = false;
        }

        if (refreshData) {
            Scheduler.schedule(level, level.getGameTime() + 2, level2 -> {
                level2.getBlockEntity(pos, BlockEntityType.BANNER).ifPresent(bbe -> {
                    bbe.fromItem(letter, letterColour);
                    ClientboundBlockEntityDataPacket update = bbe.getUpdatePacket();
                    for (ServerPlayer watching : PlayerLookup.tracking(bbe)) {
                        watching.connection.send(update);
                    }
                });
            });
        } else {
            level.getBlockEntity(pos, BlockEntityType.BANNER).ifPresent(bbe -> bbe.fromItem(letter, letterColour));
        }

        session.refresh();

        if (session.hasFinished()) {
            removeAndTerminate(player);
            Sounds.success(player);
        } else {
            Sounds.ding(player);
        }
    }
}
