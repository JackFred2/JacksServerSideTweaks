package red.jackf.jsst.features.bannerwriter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.features.Feature;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.BannerUtils;
import red.jackf.jsst.util.DelayedRunnables;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static red.jackf.jsst.command.CommandUtils.*;

public class BannerWriter extends Feature<Feature.Config> {
    private static final Map<DyeColor, BannerItem> COLOUR_TO_ITEM = new HashMap<>();
    static {
        COLOUR_TO_ITEM.put(DyeColor.WHITE, (BannerItem) Items.WHITE_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.LIGHT_GRAY, (BannerItem) Items.LIGHT_GRAY_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.GRAY, (BannerItem) Items.GRAY_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.BLACK, (BannerItem) Items.BLACK_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.RED, (BannerItem) Items.RED_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.ORANGE, (BannerItem) Items.ORANGE_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.YELLOW, (BannerItem) Items.YELLOW_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.LIME, (BannerItem) Items.LIME_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.GREEN, (BannerItem) Items.GREEN_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.CYAN, (BannerItem) Items.CYAN_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.LIGHT_BLUE, (BannerItem) Items.LIGHT_BLUE_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.BLUE, (BannerItem) Items.BLUE_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.PURPLE, (BannerItem) Items.PURPLE_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.MAGENTA, (BannerItem) Items.MAGENTA_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.PINK, (BannerItem) Items.PINK_BANNER);
        COLOUR_TO_ITEM.put(DyeColor.BROWN, (BannerItem) Items.BROWN_BANNER);
    }
    // map from characters to PMC banner codes, a bit more manageable
    // colours are black background, white text
    private static final Map<Character, String> ALPHABET = new LinkedHashMap<>(100);
    static {
        ALPHABET.put('a', "1gugsgtgv13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/letters
        ALPHABET.put('b', "1gugogv18gsgt13");
        ALPHABET.put('c', "1gvgogu1tgs13");
        ALPHABET.put('d', "1gugogv18gs13");
        ALPHABET.put('e', "1gsgvgtgo13");
        ALPHABET.put('f', "1gt1ugvgs13");
        ALPHABET.put('g', "1gu1dgogsgv13");
        ALPHABET.put('h', "g1v1ogsgu13");
        ALPHABET.put('i', "1gpgvgo13");
        ALPHABET.put('j', "1gs1dgogu13");
        ALPHABET.put('k', "1gr1dgqgs13");
        ALPHABET.put('l', "1gogs13");
        ALPHABET.put('m', "1gz1xgsgu13");
        ALPHABET.put('n', "1gs1zgrgu13");
        ALPHABET.put('o', "1gsgugogv13");
        ALPHABET.put('p', "1gu1Dgtgvgs13");
        ALPHABET.put('q', "g1ggugsgk13");
        ALPHABET.put('r', "1gd1pgvgsgr13");
        ALPHABET.put('s', "g1g1tgr13");
        ALPHABET.put('t', "1gvgp13");
        ALPHABET.put('u', "1gogsgu13");
        ALPHABET.put('v', "1gqgs1ygq13");
        ALPHABET.put('w', "1gy1wgsgu13");
        ALPHABET.put('x', "1g713");
        ALPHABET.put('y', "1gr1Dgq13");
        ALPHABET.put('z', "1gvgqgo13");
        ALPHABET.put('0', "1gogsgvgugq13"); // Credits: https://www.gamergeeks.net/apps/minecraft/banners/numbers
        ALPHABET.put('1', "1gpgl18go13");
        ALPHABET.put('2', "1gv1ggogq13");
        ALPHABET.put('3', "1gogtgv18gu13");
        ALPHABET.put('4', "1gs1Dgugt13");
        ALPHABET.put('5', "1go1ggvgr13");
        ALPHABET.put('6', "1gogu1dgtgvgs13");
        ALPHABET.put('7', "1gqgv13");
        ALPHABET.put('8', "1gvgsgtgogu13");
        ALPHABET.put('9', "1gs1Dgtgvgugo13");
        ALPHABET.put(' ', "1"); // custom from this point
        ALPHABET.put('.', "g1u1s1d161315");
        ALPHABET.put(',', "g1u1s1d1319");
        ALPHABET.put('+', "1gn1v1o13");
        ALPHABET.put('-', "1gt13");
        ALPHABET.put('*', "1gh1d13");
        ALPHABET.put('/', "1gq1318");
        ALPHABET.put('\'', "1gp1D1h1315");
        ALPHABET.put('"', "g131D1p161t");
        ALPHABET.put('=', "g131v1o1t");
        ALPHABET.put('!', "g131s1u1t1o");
        ALPHABET.put(':', "g1t1s1u1o1v");
    }
    private static final String TAG = "jsst_banner_writer_text";

    @Override
    public void init() {}

    @Override
    public String id() {
        return "bannerWriter";
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        var wrapper = CommandUtils.wrapper(this);
        node.then(
                Commands.literal("start").then(
                        Commands.argument("text", StringArgumentType.greedyString())
                                .executes(wrapper.wrap(BannerWriter::start))
                )
        ).then(
                Commands.literal("supported")
                        .executes(ctx -> {
                            var builder = new StringBuilder();
                            ALPHABET.keySet().forEach(builder::append);
                            ctx.getSource().sendSuccess(line(TextType.INFO, variable(builder.toString())), false);
                            return 0;
                        })
        ).then(Commands.literal("stop")
                .executes(ctx -> {
                    var stack = getValidBannerStack(ctx.getSource().getPlayer());
                    var tag = stack.getTag();
                    if (tag != null && tag.contains(TAG, Tag.TAG_STRING)) {
                        tag.remove(TAG);
                        ctx.getSource().sendSuccess(line(TextType.SUCCESS, text("removed banner mark")), false);
                        return 1;
                    } else {
                        ctx.getSource().sendFailure(line(TextType.ERROR, text("banner not marked for writing")));
                        return 0;
                    }
                }));
    }

    private static int start(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var stack = getValidBannerStack(ctx.getSource().getPlayer());
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(CommandUtils.line(TextType.ERROR, text("no blank banners in hand!")));
            return 0;
        }
        var str = ctx.getArgument("text", String.class).trim();
        stack.getOrCreateTag().putString(TAG, str);
        ctx.getSource().sendSuccess(CommandUtils.line(TextType.SUCCESS, text("marked banners to write "), variable(str)), false);
        var player = ctx.getSource().getPlayerOrException();
        if (!player.gameMode.isCreative() && stack.getCount() < str.length())
            ctx.getSource().sendSuccess(CommandUtils.line(TextType.INFO, text("warning: you have less banners than characters")), false);
        var unknown = new StringBuilder();
        for (var c : str.toLowerCase().toCharArray()) {
            if (!ALPHABET.containsKey(c)) {
                unknown.append(c);
            }
        }
        var unknownStr = unknown.toString();
        if (unknownStr.length() > 0) {
            ctx.getSource().sendSuccess(CommandUtils.line(TextType.INFO, text("unmapped characters, will be replaced with blank space: "), variable(unknownStr)), false);
        }
        Sounds.success(ctx.getSource().getPlayer());
        return 1;
    }

    private static boolean isValidBanner(ItemStack stack) {
        return stack.getItem() instanceof BannerItem && BannerBlockEntity.getItemPatterns(stack) == null;
    }

    private static ItemStack getValidBannerStack(ServerPlayer player) {
        if (player == null) return ItemStack.EMPTY;
        var stack = player.getItemInHand(player.getUsedItemHand());
        return isValidBanner(stack) ? stack : ItemStack.EMPTY;
    }

    // Called on banner place
    public static void onBannerPlaced(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!JSST.CONFIG.get().bannerWriter.enabled) return;
        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_STRING)) return;
        var str = tag.getString(TAG);
        if (str.length() == 0) {
            tag.remove(TAG);
            return;
        }
        if (!BannerWriter.isValidBanner(stack)) return;
        var be = level.getBlockEntity(pos);
        // valid, change patterns
        if (stack.getItem() instanceof BannerItem bannerItem && be instanceof BannerBlockEntity bannerBe && placer instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
            var translation = new HashMap<DyeColor, DyeColor>(2);
            translation.put(DyeColor.BLACK, bannerItem.getColor());
            if (bannerItem.getColor() == DyeColor.WHITE) translation.put(DyeColor.WHITE, DyeColor.BLACK);
            var pattern = BannerUtils.colourSwapPMC(ALPHABET.getOrDefault(Character.toLowerCase(str.charAt(0)), "1"), translation);
            var banner = BannerUtils.fromPMCCode(pattern);
            if (banner == null) return;
            var newBaseColour = ((BannerItem) banner.getItem()).getColor();
            if (newBaseColour != bannerBe.getBaseColor()) {
                DelayedRunnables.schedule(serverLevel, level.getGameTime() + 1, () -> {
                    replaceBannerColour(level, pos, state, COLOUR_TO_ITEM.getOrDefault(newBaseColour, COLOUR_TO_ITEM.get(DyeColor.BLACK)));
                    if (level.getBlockEntity(pos) instanceof BannerBlockEntity bannerBe2) {
                        bannerBe2.fromItem(banner);
                        // doesn't immediately update, at least for servers
                        player.connection.send(bannerBe2.getUpdatePacket());
                    }
                });
            } else {
                bannerBe.fromItem(banner);
            }
            if (str.length() == 1) {
                tag.remove(TAG);
                if (tag.isEmpty()) stack.setTag(null);
                Sounds.success(player);
                player.sendSystemMessage(line(TextType.SUCCESS, text("complete!")));
            } else {
                tag.putString(TAG, str.substring(1));
                Sounds.interact(player);
            }
        }
    }

    private static void replaceBannerColour(Level level, BlockPos pos, BlockState state, BannerItem item) {
        BlockState newState;
        if (state.getBlock() instanceof WallBannerBlock) {
            newState = item.wallBlock.defaultBlockState().setValue(WallBannerBlock.FACING, state.getValue(WallBannerBlock.FACING));
        } else {
            newState = item.getBlock().defaultBlockState().setValue(BannerBlock.ROTATION, state.getValue(BannerBlock.ROTATION));
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().bannerWriter;
    }

    public static class Config extends Feature.Config {

    }
}
