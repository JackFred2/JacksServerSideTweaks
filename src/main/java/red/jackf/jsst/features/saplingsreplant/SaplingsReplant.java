package red.jackf.jsst.features.saplingsreplant;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaplingsReplant extends Feature<SaplingsReplant.Config> {
    private static final int[] verticalOffsets = new int[]{0, 1, -1};

    // Generates a spiral outwards from Vec3i.ZERO, but not including ZERO.
    private static List<Vec3i> getSpiral(int radius) {
        var size = radius * 2 + 1;
        size *= size;
        var list = new ArrayList<Vec3i>(size);
        //list.add(Vec3i.ZERO);
        int mul = 1;
        var current = Vec3i.ZERO;
        for (int step = 1; step <= radius * 2; step++) {
            for (int i = 0; i < step; i++) {
                current = current.offset(mul, 0, 0);
                list.add(current);
            }
            for (int i = 0; i < step; i++) {
                current = current.offset(0, 0, mul);
                list.add(current);
            }
            mul *= -1;
            if (step == radius * 2)
                for (int i = 0; i < step; i++) {
                    current = current.offset(mul, 0, 0);
                    list.add(current);
                }
        }
        return list;
    }

    // Checks if this sapling is far away enough from other saplings in the configured radius. Assumes spacing is enabled
    private static boolean isClear(ServerLevel level, BlockPos pos) {
        var range = JSST.CONFIG.get().saplingsReplant.minimumDistance;
        for (int x = -range; x < range + 1; x++)
            for (int z = -range; z < range + 1; z++)
                if (level.getBlockState(pos.offset(x, 0, z)).getBlock() instanceof SaplingBlock) return false;
        return true;
    }

    // checks if the border of a suspected 2x2 is clear:
    // ? ? ? ?   ?: checked block
    // . x x ?   x: suspected 2x2
    // . o x ?   o: base pos
    // . . . ?
    private static boolean is2x2BorderClear(ServerLevel level, BlockPos base, SaplingBlock block, int xMin, int xMax, int zMin, int zMax) {
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                if (Math.abs(x) < 2 && Math.abs(z) < 2) continue;
                var pos = base.offset(x, 0, z);
                if (level.getBlockState(pos).is(block)) return false;
            }
        }

        return true;
    }

    // checks if placing a sapling at a given position could plausibly complete a 2x2 without touching another
    private static boolean couldComplete2x2(ServerLevel level, BlockPos base, SaplingBlock block) {
        var neighbours = getSpiral(1).stream().filter(p -> level.getBlockState(base.offset(p)).is(block)).collect(Collectors.toList());
        if (neighbours.size() > 3) return false;
        neighbours.add(Vec3i.ZERO);
        var xs = neighbours.stream().collect(Collectors.summarizingInt(Vec3i::getX));
        var xRange = xs.getMax() - xs.getMin();
        var zs = neighbours.stream().collect(Collectors.summarizingInt(Vec3i::getZ));
        var zRange = zs.getMax() - zs.getMin();
        if (xRange == 2 || zRange == 2) return false;

        var xMin = xs.getMin() - 1;
        var xMax = xs.getMax() + 1;
        var zMin = zs.getMin() - 1;
        var zMax = zs.getMax() + 1;

        // expand range to avoid situations like:
        // . . # #
        // . . # #
        // # . . .
        // # # . .
        // this is ran when the one neighbour is directly adjacent to base; 2x2 could theoretically be either side, so try
        // both
        if (neighbours.size() == 1) {
            if (xRange == 0) {
                return is2x2BorderClear(level, base, block, xMin - 1, xMax, zMin, zMax)
                        || is2x2BorderClear(level, base, block, xMin, xMax + 1, zMin, zMax);
            } else if (zRange == 0) {
                return is2x2BorderClear(level, base, block, xMin, xMax, zMin - 1, zMax)
                        || is2x2BorderClear(level, base, block, xMin, xMax, zMin, zMax + 1);
            }
        }

        return is2x2BorderClear(level, base, block, xMin, xMax, zMin, zMax);
    }

    // Obtains a valid position for a sapling to be placed
    private static @Nullable BlockPos getValidPos(ServerLevel level, BlockPos base, SaplingBlock sapling) {
        var spiral = getSpiral(JSST.CONFIG.get().saplingsReplant.searchRange);
        spiral.add(0, Vec3i.ZERO);
        var state = sapling.defaultBlockState();

        // try and make a 2x2
        if (sapling == Blocks.DARK_OAK_SAPLING && JSST.CONFIG.get().saplingsReplant.spacingEnabled) {
            var bigSpiral = getSpiral(JSST.CONFIG.get().saplingsReplant.searchRange + 1);
            bigSpiral.add(0, Vec3i.ZERO);
            for (int heightOffset : verticalOffsets) {
                for (var horizontalOffset : bigSpiral) {
                    var pos = base.offset(horizontalOffset).above(heightOffset);
                    if (level.getBlockState(pos).canBeReplaced() && sapling.canSurvive(state, level, pos) && couldComplete2x2(level, pos, sapling)) {
                        return pos;
                    }
                }
            }
        }
        for (int heightOffset : verticalOffsets) {
            for (var horizontalOffset : spiral) {
                var pos = base.offset(horizontalOffset).above(heightOffset);
                if (level.getBlockState(pos).canBeReplaced() && sapling.canSurvive(state, level, pos)) {
                    if (!JSST.CONFIG.get().saplingsReplant.spacingEnabled || isClear(level, pos)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    // Called when an itemstack is about to despawn
    public static void onItemDespawn(ItemEntity item) {
        if (!JSST.CONFIG.get().saplingsReplant.enabled) return;
        if (item.level instanceof ServerLevel level && item.getItem().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock sapling) {
            for (int i = 0; i <  Math.min(JSST.CONFIG.get().saplingsReplant.maxPerStack, item.getItem().getCount()); i++) {
                var pos = getValidPos(level, item.blockPosition(), sapling);
                if (pos == null) return;
                var state = sapling.defaultBlockState();
                level.setBlock(pos, state, Block.UPDATE_ALL);
                level.playSound(null, pos, sapling.getSoundType(state).getPlaceSound(), SoundSource.BLOCKS);
            }
        }
    }

    @Override
    public void init() {
        getConfig().minimumDistance = Mth.clamp(getConfig().minimumDistance, 1, 3);
        getConfig().searchRange = Mth.clamp(getConfig().searchRange, 1, 4);
        getConfig().maxPerStack = Mth.clamp(getConfig().maxPerStack, 1, 64);
    }

    @Override
    public String id() {
        return "saplingsReplant";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().saplingsReplant;
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        node.then(OptionBuilders.withIntRange("minimumDistance", 1, 3, () -> getConfig().minimumDistance, i -> getConfig().minimumDistance = i))
                .then(OptionBuilders.withIntRange("searchRange", 1, 4, () -> getConfig().searchRange, i -> getConfig().searchRange = i))
                .then(OptionBuilders.withIntRange("maxPerStack", 1, 64, () -> getConfig().maxPerStack, i -> getConfig().maxPerStack = i))
                .then(OptionBuilders.withBoolean("spacingEnabled", () -> getConfig().spacingEnabled, b -> getConfig().spacingEnabled = b));
    }

    public static class Config extends Feature.Config {
        @Comment("Should saplings try to space themself out? (Default: true, Options: true, false)")
        public boolean spacingEnabled = true;

        @Comment("Minimum blocks between saplings. Requires spacingEnabled. (Default: 2, Range: [1, 3])")
        public int minimumDistance = 1;

        @Comment("Horizontal distance that saplings search for a valid position. Vertically, always checks layer above and below. (Default: 3, Range: [1, 4])")
        public int searchRange = 3;

        @Comment("Maximum number of saplings to plant per dropped stack; the rest are discarded. (Default: 5, Range: [1, 64])")
        public int maxPerStack = 5;
    }
}
