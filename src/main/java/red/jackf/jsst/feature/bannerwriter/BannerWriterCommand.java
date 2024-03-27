package red.jackf.jsst.feature.bannerwriter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.command.Formatting;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.banners.Banners;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BannerWriterCommand {
    public static final Predicate<CommandSourceStack> PREDICATE = ctx -> ctx.isPlayer() && (ctx.hasPermission(4) || !BannerWriter.INSTANCE.config().operatorOnly);

    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext buildCtx) {
        var root = Commands.literal("bannerWriter");

        var colour = Commands.argument("textColour", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(DyeColor.values()).map(DyeColor::getName), builder));

        var text = Commands.argument("text", StringArgumentType.greedyString())
                .suggests(BannerWriterCommand::textUnknownWarning)
                .executes(ctx -> {
                    String textColourText = StringArgumentType.getString(ctx, "textColour");
                    DyeColor textColour = DyeColor.byName(textColourText, null);
                    if (textColour == null)
                        throw ColorArgument.ERROR_INVALID_VALUE.create(textColourText);

                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    Optional<DyeColor> backgroundColour = getColourFromStack(player.getItemInHand(InteractionHand.MAIN_HAND))
                            .or(() -> getColourFromStack(player.getItemInHand(InteractionHand.OFF_HAND)));
                    if (backgroundColour.isEmpty()) {
                        ctx.getSource().sendFailure(Formatting.errorLine(Component.translatable("jsst.bannerWriter.needBanners")));
                        return 0;
                    }

                    Characters.Processed processed = Characters.INSTANCE.process(StringArgumentType.getString(ctx, "text"));

                    processed.invalid().ifPresent(invalid -> ctx.getSource().sendSystemMessage(Formatting.errorLine(Component.translatable(
                            "jsst.bannerWriter.invalid", Component.literal(invalid).withStyle(Styles.EXAMPLE)
                    ))));

                    BannerWriter.INSTANCE.start(ctx.getSource().getPlayerOrException(), backgroundColour.get(), textColour, processed.text());
                    return processed.text().length();
                });

        colour.then(text);
        root.then(colour);

        return root;
    }

    private static Optional<DyeColor> getColourFromStack(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        if (!(stack.getItem() instanceof BannerItem)) return Optional.empty();
        var patterns = Banners.parseStack(stack);
        if (!patterns.patterns().isEmpty()) return Optional.empty();
        return Optional.ofNullable(patterns.baseColour());
    }

    private static CompletableFuture<Suggestions> textUnknownWarning(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String current = builder.getInput().substring(builder.getStart());

        Characters.Processed processed = Characters.INSTANCE.process(current);
        if (processed.invalid().isPresent()) {
            builder.suggest(processed.text(), Component.translatable("jsst.bannerWriter.invalid", processed.invalid().get()));
        } else {
            builder.suggest(processed.text());
        }

        return CompletableFuture.completedFuture(builder.build());
    }
}
