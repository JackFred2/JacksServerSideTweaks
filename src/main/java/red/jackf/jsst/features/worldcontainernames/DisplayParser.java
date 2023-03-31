package red.jackf.jsst.features.worldcontainernames;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DisplayParser {
    private static final Map<Predicate<BlockEntity>, Parser> parsers = new HashMap<>();
    private static final ItemStack UNKNOWN = new ItemStack(Items.BARRIER);

    @Nullable
    private static ItemStack getItem(BlockEntity be, Component text) {
        if (text == null) return null;
        var rawText = text.getString();
        if (rawText.startsWith("[item:") && rawText.endsWith("]")) {
            var reader = new StringReader(rawText.substring(6, rawText.length() - 1));
            try {
                var id = ResourceLocation.read(reader);
                var item = BuiltInRegistries.ITEM.getOptional(id);
                if (item.isPresent()) {
                    var stack = new ItemStack(item.get());
                    if (reader.canRead() && reader.peek() == '{') {
                        var tag = new TagParser(reader).readStruct();
                        stack.setTag(tag);
                    }
                    return stack;
                } else {
                    return UNKNOWN;
                }
            } catch (CommandSyntaxException ex) {
                return UNKNOWN;
            }
        } else if (rawText.equals("[max-count]")) {
            if (be instanceof Container container) {
                var counts = new HashMap<Item, Integer>();
                // creates a blank display "displaying" air; saves some logic that would otherwise just display "[max-count]"
                counts.put(Items.AIR, 0);
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    var stack = container.getItem(slot);
                    counts.put(stack.getItem(), counts.getOrDefault(stack.getItem(), 0) + stack.getCount());
                }
                //noinspection OptionalGetWithoutIsPresent
                return new ItemStack(counts.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey());
            } else {
                return UNKNOWN;
            }
        } else return null;
    }

    private static final Parser DEFAULT = be -> {
        if (be instanceof Nameable nameable && nameable.hasCustomName()) {
            var item = getItem(be, nameable.getCustomName());
            return new DisplayData(be.getBlockPos().above().getCenter(), item == null ? nameable.getCustomName() : null, item);
        } else {
            return null;
        }
    };

    static {
        // Chest Blocks
        // Name in-gui pulled from either the half that has a name if only 1, or the side with lower coordinates
        parsers.put(be -> be.getBlockState().getOptionalValue(ChestBlock.TYPE).isPresent(), be -> {
            if (be instanceof Nameable nameable && nameable.hasCustomName()) {
                var blockState = be.getBlockState();
                var level = be.getLevel();
                if (blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE || level == null) return DEFAULT.parse(be);

                var pos = be.getBlockPos();
                var linkedDirection = ChestBlock.getConnectedDirection(blockState);
                var otherBe = level.getBlockEntity(pos.relative(linkedDirection));

                var item = getItem(be, nameable.getCustomName());
                var resultIfUs = new DisplayData(pos.above().getCenter().relative(linkedDirection, 0.5), item == null ? nameable.getCustomName() : null, item);

                if (otherBe instanceof Nameable otherNameable && otherNameable.hasCustomName()) { // could be either, check lowest coord
                    var otherPos = otherBe.getBlockPos();
                    if (pos.getX() < otherPos.getX() || pos.getZ() < otherPos.getZ()) { // use our name
                        return resultIfUs;
                    } else { // use their name
                        return null;
                    }
                } else { // use our name
                    return resultIfUs;
                }
            } else { // use no name
                return null;
            }
        });
    }

    // Gets the text and location for a block entity. Returns null if text should be removed/not made.
    public static @Nullable DisplayData parse(BlockEntity be) {
        return parsers.entrySet().stream()
                .filter(entry -> entry.getKey().test(be))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(DEFAULT)
                .parse(be);
    }

    public record DisplayData(Vec3 pos, @Nullable Component text, @Nullable ItemStack stack) {
        public DisplayData {
            assert (text == null) != (stack == null);
        }

        public boolean isText() {
            return text != null;
        }

        public boolean matches(@Nullable Display display) {
            if (isText()) return display instanceof Display.TextDisplay;
            else return display instanceof Display.ItemDisplay;
        }
    }

    private interface Parser {
        @Nullable
        DisplayParser.DisplayData parse(BlockEntity be);
    }
}
