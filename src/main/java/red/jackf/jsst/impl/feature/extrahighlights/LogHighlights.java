package red.jackf.jsst.impl.feature.extrahighlights;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
//? if >=1.21.4 {
import net.minecraft.world.InteractionResult;
//?} else {
/*import net.minecraft.world.InteractionResultHolder;
*///?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.RegistryUtils;
import red.jackf.jsst.impl.utils.Scheduler;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.Versioned;

import java.util.*;
import java.util.stream.Stream;

public class LogHighlights {
    private static final ResourceLocation AFTER_DEFAULT = JSST.id("after_default");
    private static final float START_SCALE = 0.8f;
    private static final float END_SCALE = 0.4f;

    public static void setup() {
        UseItemCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_DEFAULT);
        UseItemCallback.EVENT.register(AFTER_DEFAULT, (player, level, hand) -> {
            if (player instanceof ServerPlayer serverPlayer && player.isCrouching()) {
                var config = JSSTConfig.INSTANCE.instance().extraHighlights;

                if (config.treeLogsEnabled) {
                    var axesTag = RegistryUtils.parseTag(Registries.ITEM, config.axesTag);
                    ItemStack handStack = player.getItemInHand(hand);

                    //? if >=1.21.4 {
                    //noinspection UnnecessaryLocalVariable
                    var cooldownToken = handStack;
                    //?} else {
                    /*var cooldownToken = handStack.getItem();
                    *///?}
                    if (axesTag.isPresent() && handStack.is(axesTag.get()) && !player.getCooldowns().isOnCooldown(cooldownToken)) {
                        player.getCooldowns().addCooldown(cooldownToken, config.highlightTime);
                        Sounds.Ding.ding(serverPlayer, 1.4f);

                        highlightNearbyLogs(serverPlayer);

                        //? if >=1.21.4 {
                        return InteractionResult.SUCCESS_SERVER;
                         //?} else {
                        /*return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, false);
                        *///?}
                    }
                }
            }

            return Versioned.itemInteractPass();
        });
    }

    private static void highlightNearbyLogs(ServerPlayer player) {
        var config = JSSTConfig.INSTANCE.instance().extraHighlights;
        var logsTag = RegistryUtils.parseTag(Registries.BLOCK, config.logsTag);

        if (logsTag.isPresent()) {
            int radius = config.treeLogsRange;
            int halfRadius = radius / 2;

            var startPos = player.blockPosition();
            var logs = BlockPos.MutableBlockPos.betweenClosedStream(
                    startPos.getX() - radius,
                    startPos.getY() - halfRadius,
                    startPos.getZ() - radius,
                    startPos.getX() + radius,
                    startPos.getY() + radius + halfRadius,
                    startPos.getZ() + radius
            ).filter(pos -> player.serverLevel().getBlockState(pos).is(logsTag.get()))
            .map(BlockPos::immutable)
            .toList();

            for (var entry : clusterPositions(player.serverLevel().random, logs).entrySet()) {
                var entity = EntityBuilders.blockDisplay(player.serverLevel())
                        .state(Blocks.OAK_LOG.defaultBlockState())
                        .positionCentered(entry.getKey())
                        .glowing(true, entry.getValue())
                        .scaleAndCenter(START_SCALE)
                        .transformInterpolationDuration(config.highlightTime)
                        .build();
                Scheduler.schedule(player.serverLevel(), player.serverLevel().getGameTime() + 2, level -> {
                    EntityUtils.setDisplayScale(entity, new Vector3f(END_SCALE));
                    EntityUtils.setDisplayTranslation(entity, new Vector3f(END_SCALE).mul(-0.5f));
                    EntityUtils.startInterpolationIn(entity, 0);
                });
                var lie = EntityLie.builder(entity).createAndShow(player);


                Debris.INSTANCE.schedule(lie, config.highlightTime);
            }
        }
    }

    private static Stream<BlockPos> neighbours(BlockPos in) {
        /*
        return Stream.of(
                in.above(),
                in.below(),
                in.west(),
                in.east(),
                in.south(),
                in.north()
        );*/
        return BlockPos.betweenClosedStream(in.offset(new Vec3i(-1, -1, -1)), in.offset(new Vec3i(1, 1, 1)))
                .map(BlockPos::immutable);
    }

    private static Map<BlockPos, Colour> clusterPositions(RandomSource random, List<BlockPos> positions) {
        Set<BlockPos> asSet = Set.copyOf(positions);
        Map<BlockPos, Colour> map = new HashMap<>();

        for (BlockPos position : positions) {
            if (map.containsKey(position)) continue;

            Colour colour = Colour.fromHSV(random.nextFloat(), 1f, 1f);

            SequencedSet<BlockPos> queue = new LinkedHashSet<>();
            queue.add(position);

            while (!queue.isEmpty()) {
                BlockPos next = queue.removeFirst();

                map.put(next, colour);

                neighbours(next).filter(asSet::contains)
                        .filter(pos -> !map.containsKey(pos) && !queue.contains(pos))
                        .forEach(queue::add);
            }
        }

        return map;
    }
}
