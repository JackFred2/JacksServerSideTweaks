package red.jackf.jsst.features.worldcontainernames;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

// Returns a list of blocks to also update
public class UpdateParser {
    private static final Map<Predicate<BlockEntity>, Parser> parsers = new HashMap<>();
    private static final Parser DEFAULT = be -> Collections.emptySet();

    static {
        // Chests and chest-likes: returns the other half if a double chest.
        parsers.put(be -> be.getBlockState().getOptionalValue(ChestBlock.TYPE).isPresent(), be -> {
            var type = be.getBlockState().getValue(ChestBlock.TYPE);
            if (type == ChestType.SINGLE) return  Collections.emptySet();
            return Set.of(be.getBlockPos().relative(ChestBlock.getConnectedDirection(be.getBlockState())));
        });
    }

    // Gets a list of additional positions to check aside from this.
    public static Set<BlockPos> parse(BlockEntity be) {
        return parsers.entrySet().stream()
                .filter(entry -> entry.getKey().test(be))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(DEFAULT)
                .parse(be);
    }

    private interface Parser {
        Set<BlockPos> parse(BlockEntity be);
    }
}
