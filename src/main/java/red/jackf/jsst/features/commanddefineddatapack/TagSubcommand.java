package red.jackf.jsst.features.commanddefineddatapack;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.CommandUtils;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static red.jackf.jsst.command.CommandUtils.*;

public class TagSubcommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_REGISTRY = new DynamicCommandExceptionType(obj -> Component.literal("Unknown registry: " + obj.toString()));
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_REGISTRY = (ctx, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.REGISTRY.keySet().stream(), builder);
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_TAGS = (ctx, builder) -> BuiltInRegistries.REGISTRY.getOptional(ctx.getArgument("registry", ResourceLocation.class)).map(objects -> SharedSuggestionProvider.suggestResource(objects.getTagNames().map(TagKey::location), builder)).orElseGet(Suggestions::empty);

    private static CompletableFuture<Suggestions> getValueSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) return Suggestions.empty();
        var tagStream = getTagContents(registry, ctx.getArgument("tag", ResourceLocation.class));
        if (tagStream == null) return Suggestions.empty();
        var tagContents = tagStream.collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(registry.keySet().stream().filter(resLoc -> !tagContents.contains(resLoc)), builder);
    }

    private static CompletableFuture<Suggestions> getTagSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) return Suggestions.empty();
        var tag = ctx.getArgument("tag", ResourceLocation.class);
        return SharedSuggestionProvider.suggestResource(registry.getTagNames().map(TagKey::location).filter(resLoc -> !resLoc.equals(tag)), builder);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return literal("tag").then(argument("registry", ResourceLocationArgument.id()).suggests(SUGGESTIONS_REGISTRY).then(literal("listTags").executes(ctx -> listTagsForRegistry(ctx, null)).then(argument("filter", StringArgumentType.greedyString()).executes(ctx -> listTagsForRegistry(ctx, ctx.getArgument("filter", String.class))))).then(literal("list").then(argument("tag", ResourceLocationArgument.id()).suggests(SUGGESTIONS_TAGS).executes(TagSubcommand::listTagContents))).then(literal("add").then(argument("tag", ResourceLocationArgument.id()).suggests(SUGGESTIONS_TAGS).then(literal("value").then(argument("newValue", ResourceLocationArgument.id()).suggests(TagSubcommand::getValueSuggestions).executes(TagSubcommand::addValueToTag))).then(literal("tag").then(argument("newTag", ResourceLocationArgument.id()).suggests(TagSubcommand::getTagSuggestions).executes(TagSubcommand::addTagToTag))))));
    }

    private static int addValueToTag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("tag", ResourceLocation.class);
        var newValue = ctx.getArgument("newValue", ResourceLocation.class);

        JSST.LOGGER.info("Adding " + newValue + " to " + tagId + " in " + registryId);
        return 0;
    }

    private static int addTagToTag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("tag", ResourceLocation.class);
        var newTag = ctx.getArgument("newTag", ResourceLocation.class);

        JSST.LOGGER.info("Adding #" + newTag + " to " + tagId + " in " + registryId);
        return 0;
    }

    @Nullable
    private static <T> Stream<ResourceLocation> getTagContents(Registry<T> registry, ResourceLocation tagId) {
        var tag = registry.getTag(TagKey.create(registry.key(), tagId));
        return tag.map(holders -> holders.stream().map(holder -> registry.getKey(holder.value())).filter(Objects::nonNull)).orElse(null);
    }

    private static int listTagContents(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("tag", ResourceLocation.class);
        var tagContents = getTagContents(registry, tagId);
        if (tagContents == null) { // valid, just not made yet
            ctx.getSource().sendSystemMessage(CommandUtils.errorPrefix().append(text("No tag exists by the name of ")).append(variable(tagId.toString())));
        } else {
            ctx.getSource().sendSystemMessage(CommandUtils.infoPrefix().append(text("Contents of ")).append(variable(registryId.toString()).withStyle(suggests("/jsst cdd tag " + registryId + " listTags"))).append(text(" tag ")).append(variable(tagId.toString())).append(symbol(":")));
            tagContents.forEach(resloc -> ctx.getSource().sendSystemMessage(symbol("- ").append(text(resloc.toString()))));
        }
        return 1;
    }

    private static int listTagsForRegistry(CommandContext<CommandSourceStack> ctx, @Nullable String filter) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        var registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var title = CommandUtils.infoPrefix().append(text("Tags in ")).append(variable(registryId.toString()));
        if (filter != null) title.append(text(" matching ")).append(variable(filter));
        title.append(symbol(":"));
        ctx.getSource().sendSystemMessage(title);
        var stream = registry.getTagNames();
        if (filter != null) stream = stream.filter(key -> key.location().toString().contains(filter));
        stream.sorted(Comparator.comparing(TagKey::location)).forEach(key -> ctx.getSource().sendSystemMessage(symbol("- ").append(text(key.location().toString()).withStyle(suggests( "/jsst cdd tag " + registryId + " list " + key.location())))));
        return 1;
    }
}
