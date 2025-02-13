package red.jackf.jsst.impl.feature.saplingreplant;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SaplingBlock;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.mixins.saplingreplant.SaplingBlockAccessor;

import java.util.*;
import java.util.stream.StreamSupport;

public class SaplingReplant {
    public static void setup() {

    }

    private static Set<Pair<Integer, Integer>> getSaplingMap(Level level, BlockPos root, SaplingBlock sapling, boolean is2x2) {
        var config = JSSTConfig.INSTANCE.instance().saplingReplant;

        Set<Pair<Integer, Integer>> result = new HashSet<>();
        int usedRadius = config.searchRadiusBlocks + config.minSpacing;

        for (int x = -usedRadius; x < usedRadius + (is2x2 ? 0 : 1); x++) {
            for (int z = -usedRadius; z < usedRadius + (is2x2 ? 0 : 1); z++) {
                for (int y : List.of(0, -1, 1, -2, 2)) {
                    BlockPos pos = root.offset(x, y, z);

                    if (level.getBlockState(pos).is(sapling)) {
                        result.add(new Pair<>(root.getX() + x, root.getZ() + z));
                        break;
                    }
                }
            }
        }

        return result;
    }

    private static boolean canPlant(Level level, BlockPos pos, SaplingBlock sapling, boolean allowExistingSaplings) {
        BlockState state = level.getBlockState(pos);
        return (state.canBeReplaced() || (allowExistingSaplings && state.is(sapling))) && ((SaplingBlockAccessor) sapling).invokeCanSurvive(sapling.defaultBlockState(), level, pos);
    }

    private static List<BlockPos> getOtherPositions(Level level, BlockPos root, SaplingBlock sapling) {
        if (!canPlant(level, root, sapling, true)) return Collections.emptyList();

        Direction primary = Direction.EAST;
        Direction secondary = Direction.SOUTH;

        List<BlockPos> offsets = List.of(root.relative(primary), root.relative(secondary), root.relative(primary).relative(secondary));

        if (offsets.stream().allMatch(pos -> canPlant(level, pos, sapling, true)))
            return offsets;

        return Collections.emptyList();
    }

    private static boolean isBlocked(BlockPos pos, Set<Pair<Integer, Integer>> map, Set<BlockPos> whitelist) {
        return StreamSupport.stream(BlockPos.spiralAround(pos, JSSTConfig.INSTANCE.instance().saplingReplant.minSpacing, Direction.EAST, Direction.SOUTH)
                        .spliterator(), false)
                .anyMatch(pos2 -> !whitelist.contains(pos2) && map.contains(Pair.of(pos2.getX(), pos2.getZ())));
    }

    public static void possiblyReplant(ItemEntity entity) {
        if (entity.getItem().getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof SaplingBlock saplingBlock
                && entity.level() instanceof ServerLevel level) {
            boolean is2x2 = ((JSSTTreeGrower) (Object) ((SaplingBlockAccessor) saplingBlock).getTreeGrower()).jsst$saplingreplant$is2x2Only();
            Set<Pair<Integer, Integer>> saplingMap = getSaplingMap(level, entity.blockPosition(), saplingBlock, is2x2);

            // nearby positions in spiral order that can hold this sapling
            Set<BlockPos> placed = new HashSet<>();

            for (int i = 0; i < Math.min(JSSTConfig.INSTANCE.instance().saplingReplant.maxPerStack, entity.getItem().getCount()); i++) {
                boolean placedAndCompleted2x2 = false;

                check2x2:
                if (is2x2) { // prioritise completing existing 2x2
                    var partialViable2x2s = StreamSupport.stream(BlockPos.spiralAround(entity.blockPosition(), JSSTConfig.INSTANCE.instance().saplingReplant.searchRadiusBlocks, Direction.EAST, Direction.SOUTH)
                            .spliterator(), false)
                            .filter(pos -> level.getBlockState(pos).is(saplingBlock))
                            .map(pos -> Pair.of(pos.immutable(), getOtherPositions(level, pos, saplingBlock)))
                            .filter(pair -> !pair.getSecond().isEmpty())
                            .toList();


                    for (var partial : partialViable2x2s) {
                        var this2x2 = new HashSet<>(partial.getSecond());
                        this2x2.add(partial.getFirst());

                        boolean valid = this2x2.stream()
                                .noneMatch(pos -> !canPlant(level, pos, saplingBlock, true) || isBlocked(pos, saplingMap, this2x2));

                        if (!valid) continue;

                        List<BlockPos> toCheck = this2x2.stream().filter(pos -> !placed.contains(pos) && !saplingMap.contains(Pair.of(pos.getX(), pos.getZ()))).toList();

                        for (var otherPosition : toCheck) {
                            placeInWorld(level, otherPosition, saplingBlock, saplingMap, placed);
                            placedAndCompleted2x2 = true;
                            break check2x2;
                        }
                    }
                }

                if (placedAndCompleted2x2) continue;

                List<BlockPos> positions = new ArrayList<>();
                for (int offset : List.of(0, -1, 1)) {
                    StreamSupport.stream(BlockPos.spiralAround(entity.blockPosition().offset(0, offset, 0), JSSTConfig.INSTANCE.instance().saplingReplant.searchRadiusBlocks, Direction.EAST, Direction.SOUTH).spliterator(), false)
                            .filter(pos -> canPlant(level, pos, saplingBlock, false))
                            .map(BlockPos.MutableBlockPos::immutable)
                            .forEach(positions::add);
                }

                for (BlockPos pos : positions) {
                    if (placed.contains(pos)) continue;

                    List<BlockPos> toCheck = new ArrayList<>();
                    toCheck.add(pos);

                    if (is2x2) {
                        var others = getOtherPositions(level, pos, saplingBlock);
                        if (others.isEmpty()) continue;
                        toCheck.addAll(others);
                    }

                    boolean isBlocked = false;

                    for (var pos2 : toCheck) {
                        isBlocked |= isBlocked(pos2, saplingMap, Set.of());
                    }

                    if (isBlocked) continue;

                    placeInWorld(level, pos, saplingBlock, saplingMap, placed);
                    break;
                }
            }
        }
    }

    private static void placeInWorld(ServerLevel level, BlockPos position, SaplingBlock sapling, Set<Pair<Integer, Integer>> saplingMap, Set<BlockPos> placed) {
        BlockState state = sapling.defaultBlockState();
        level.setBlockAndUpdate(position, state);
        SoundType soundType = state.getSoundType();
        level.playSound(null, position, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        saplingMap.add(Pair.of(position.getX(), position.getZ()));
        placed.add(position);
    }
}
