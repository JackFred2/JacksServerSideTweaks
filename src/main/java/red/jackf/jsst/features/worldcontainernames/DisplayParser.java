package red.jackf.jsst.features.worldcontainernames;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DisplayParser {
    private static final Map<Predicate<BlockEntity>, Parser> parsers = new HashMap<>();

    private static final Parser DEFAULT = be -> {
        if (be instanceof Nameable nameable && nameable.hasCustomName()) {
            return new DisplayData(be.getBlockPos().above().getCenter(), nameable.getCustomName());
        } else {
            return null;
        }
    };

    static {
        // Chest Blocks
        // Name in-gui pulled from either the half that has a name if only 1, or the side with lower coordinates
        parsers.put(be -> be.getBlockState().getOptionalValue(ChestBlock.TYPE).isPresent(), be -> {
            if (be instanceof Nameable nameable && nameable.hasCustomName()) {
                var pos = be.getBlockPos();
                var blockState = be.getBlockState();
                var level = be.getLevel();
                if (blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE || level == null) return DEFAULT.parse(be);
                var linkedDirection = ChestBlock.getConnectedDirection(blockState);
                var otherBe = level.getBlockEntity(pos.relative(linkedDirection));
                if (otherBe instanceof Nameable otherNameable && otherNameable.hasCustomName()) { // could be either, check lowest coord
                    var otherPos = otherBe.getBlockPos();
                    if (pos.getX() < otherPos.getX() || pos.getZ() < otherPos.getZ()) { // use our name
                        return new DisplayData(pos.above().getCenter().relative(linkedDirection, 0.5), nameable.getCustomName());
                    } else { // use their name
                        return null;
                    }
                } else { // use our name
                    return new DisplayData(pos.above().getCenter().relative(linkedDirection, 0.5), nameable.getCustomName());
                }
            } else { // use no name
                return null;
            }
        });
    }

    // Gets the text and location for a block entity. Returns null if should be removed/not made.
    @Nullable
    public static DisplayParser.DisplayData parse(BlockEntity be) {
        return parsers.entrySet().stream()
                .filter(entry -> entry.getKey().test(be))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(DEFAULT)
                .parse(be);
    }

    public record DisplayData(Vec3 pos, Component text) {}

    private interface Parser {
        @Nullable
        DisplayParser.DisplayData parse(BlockEntity be);
    }
}
