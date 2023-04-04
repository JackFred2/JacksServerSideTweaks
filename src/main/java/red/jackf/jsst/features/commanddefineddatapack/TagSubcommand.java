package red.jackf.jsst.features.commanddefineddatapack;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.command.CommandUtils;
import red.jackf.jsst.command.EnabledWrapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static red.jackf.jsst.command.CommandUtils.*;

public class TagSubcommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_REGISTRY = new DynamicCommandExceptionType(obj -> Component.literal("Unknown registry: " + obj.toString()));
    private static final DynamicCommandExceptionType ERROR_NO_DATAPACK_TAG = new DynamicCommandExceptionType(obj -> Component.literal("No such datapack tag exists: " + obj.toString()));
    private static final DynamicCommandExceptionType ERROR_NO_SUCH_ELEMENT = new DynamicCommandExceptionType(obj -> Component.literal("No such element exists: " + obj.toString()));
    private static final DynamicCommandExceptionType ERROR_ELEMENT_ALREADY_EXISTS = new DynamicCommandExceptionType(obj -> Component.literal("Element already present: " + obj.toString()));
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_REGISTRY = (ctx, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.REGISTRY.keySet()
            .stream(), builder);
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_TAGS = (ctx, builder) -> BuiltInRegistries.REGISTRY.getOptional(ctx.getArgument("registry", ResourceLocation.class))
            .map(objects -> SharedSuggestionProvider.suggestResource(objects.getTagNames()
                    .map(TagKey::location), builder)).orElseGet(Suggestions::empty);

    private static <T> Collection<ResourceLocation> getDatapackTags(Registry<T> registry) {
        if (CommandDefinedDatapack.currentState != null)
            return CommandDefinedDatapack.currentState.getTags().get(registry.key()).keySet();
        return Collections.emptyList();
    }
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_DATAPACK_TAGS = (ctx, builder) -> {
        if (CommandDefinedDatapack.currentState != null) {
            var reg = BuiltInRegistries.REGISTRY.get(ctx.getArgument("registry", ResourceLocation.class));
            if (reg != null) {
                return SharedSuggestionProvider.suggestResource(getDatapackTags(reg).stream(), builder);
            }
        }
        return Suggestions.empty();
    };

    // Suggests items to remove from a tag
    private static CompletableFuture<Suggestions> getDatapackElementSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        if (CommandDefinedDatapack.currentState == null) return Suggestions.empty();
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) return Suggestions.empty();
        var datapackTagsForRegistry = CommandDefinedDatapack.currentState.getTags().get(registry.key());
        if (datapackTagsForRegistry == null) return Suggestions.empty();
        var tag = datapackTagsForRegistry.get(ctx.getArgument("datapackTag", ResourceLocation.class));
        if (tag == null) return Suggestions.empty();
        return SharedSuggestionProvider.suggest(tag.entries().stream().map(TagEntry::toString), builder);
    }

    // Suggests new items to add to a tag, excluding any that are already present
    private static CompletableFuture<Suggestions> getElementSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) return Suggestions.empty();
        var tagStream = getTagContents(registry, ctx.getArgument("to", ResourceLocation.class));
        if (tagStream == null) return Suggestions.empty();
        var tagContents = tagStream.collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(registry.keySet().stream()
                .filter(resLoc -> !tagContents.contains(resLoc)), builder);
    }

    // Suggests new tags to add to a tag, excluding the tag being edited
    private static CompletableFuture<Suggestions> getTagSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) return Suggestions.empty();
        var tag = ctx.getArgument("to", ResourceLocation.class);
        return SharedSuggestionProvider.suggestResource(registry.getTagNames().map(TagKey::location)
                .filter(resLoc -> !resLoc.equals(tag)), builder);
    }

    // Generate command tree for tag editing
    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandDefinedDatapack cdd) {
        var wrapper = new EnabledWrapper(cdd);
        return literal("tag")
                .then(argument("registry", ResourceLocationArgument.id()).suggests(SUGGESTIONS_REGISTRY)
                        .then(literal("listTags")
                                .executes(wrapper.wrap(ctx -> listTagsForRegistry(ctx, null)))
                                .then(argument("filter", StringArgumentType.greedyString())
                                        .executes(wrapper.wrap(ctx -> listTagsForRegistry(ctx, ctx.getArgument("filter", String.class))))))
                        .then(literal("list")
                                .then(argument("tag", ResourceLocationArgument.id())
                                        .suggests(SUGGESTIONS_TAGS)
                                        .executes(wrapper.wrap(TagSubcommand::listTagContents))))
                        .then(literal("add")
                                .then(argument("to", ResourceLocationArgument.id())
                                        .suggests(SUGGESTIONS_TAGS)
                                        .then(literal("value")
                                                .then(argument("newElement", ResourceLocationArgument.id())
                                                        .suggests(TagSubcommand::getElementSuggestions)
                                                        .executes(wrapper.wrap(ctx -> TagSubcommand.addElementToTag(ctx, false)))
                                                        .then(literal("optional")
                                                                .executes(wrapper.wrap(ctx -> TagSubcommand.addElementToTag(ctx, true))))))
                                        .then(literal("tag")
                                                .then(argument("newTag", ResourceLocationArgument.id())
                                                        .suggests(TagSubcommand::getTagSuggestions)
                                                        .executes(wrapper.wrap(ctx -> TagSubcommand.addTagToTag(ctx, false)))
                                                        .then(literal("optional")
                                                                .executes(wrapper.wrap(ctx -> TagSubcommand.addTagToTag(ctx, true))))))))
                        .then(literal("remove")
                                .then(argument("datapackTag", ResourceLocationArgument.id())
                                        .suggests(SUGGESTIONS_DATAPACK_TAGS)
                                        .then(argument("elementToRemove", StringArgumentType.greedyString())
                                                .suggests(TagSubcommand::getDatapackElementSuggestions)
                                                .executes(wrapper.wrap(TagSubcommand::removeFromTag)))))
                        .then(literal("setReplace")
                                .then(argument("datapackTag", ResourceLocationArgument.id())
                                        .suggests(SUGGESTIONS_DATAPACK_TAGS)
                                        .then(argument("shouldReplace", BoolArgumentType.bool())
                                                .executes(wrapper.wrap(TagSubcommand::setReplace))))));
    }

    private static int setReplace(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var state = CommandDefinedDatapack.currentState;
        if (state == null) throw new CommandRuntimeException(errorPrefix().append(text("PackState not loaded!")));
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("datapackTag", ResourceLocation.class);
        var datapackTagsForRegistry = state.getTags().get(registry.key());
        if (datapackTagsForRegistry == null) throw ERROR_NO_DATAPACK_TAG.create(tagId);
        TagFile tagFile = datapackTagsForRegistry.get(tagId);
        if (tagFile == null) throw ERROR_NO_DATAPACK_TAG.create(tagId);
        var replace = ctx.getArgument("shouldReplace", Boolean.class);
        datapackTagsForRegistry.put(tagId, new TagFile(tagFile.entries(), replace));
        ctx.getSource().sendSuccess(successPrefix().append(text("Marked ")).append(variable(tagId.toString())).append(text(" to " + (replace ? "" : "not ") + "replace other tags.")), true);
        state.save();
        return 1;
    }

    // Remove an tag element from a tag file via string comparison
    private static boolean removeElement(TagFile in, String element) {
        for (int i = 0; i < in.entries().size(); i++)
            if (in.entries().get(i).toString().equals(element)) {
                in.entries().remove(i);
                return true;
            }
        return false;
    }

    // Command: Remove an entry from a datapack tag file
    private static int removeFromTag(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var state = CommandDefinedDatapack.currentState;
        if (state == null) throw new CommandRuntimeException(errorPrefix().append(text("PackState not loaded!")));
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("datapackTag", ResourceLocation.class);
        var datapackTagsForRegistry = state.getTags().get(registry.key());
        if (datapackTagsForRegistry == null) throw ERROR_NO_DATAPACK_TAG.create(tagId);
        TagFile tagFile = datapackTagsForRegistry.get(tagId);
        if (tagFile == null) throw ERROR_NO_DATAPACK_TAG.create(tagId);
        var toRemove = ctx.getArgument("elementToRemove", String.class);
        if (!removeElement(tagFile, toRemove)) throw ERROR_NO_SUCH_ELEMENT.create(toRemove);
        ctx.getSource().sendSuccess(successPrefix().append(text("Removed ")).append(variable(toRemove)).append(text(" from ")).append(variable(tagId.toString())).append(text(".")), true);
        if (tagFile.entries().size() == 0) {
            datapackTagsForRegistry.remove(tagId, tagFile);
            ctx.getSource().sendSuccess(successPrefix().append(variable(tagId.toString())).append(text(" is now empty, removing.")), true);
        }
        state.save();
        return 1;
    }

    // Add an entry to a datapack tag file
    private static <T> void addToTag(Registry<T> registry, ResourceLocation tagId, TagEntry newEntry) throws CommandRuntimeException, CommandSyntaxException {
        var state = CommandDefinedDatapack.currentState;
        if (state == null) throw new CommandRuntimeException(errorPrefix().append(text("PackState not loaded!")));
        var tagFile = state.getTags().computeIfAbsent(registry.key(), key -> new HashMap<>())
                .computeIfAbsent(tagId, resLoc -> new TagFile(new ArrayList<>(), false));
        if (tagFile.entries().stream().anyMatch(e -> e.toString().equals(newEntry.toString()))) throw ERROR_ELEMENT_ALREADY_EXISTS.create(newEntry.toString());
        tagFile.entries().add(newEntry);
        state.save();
    }

    // Command: Add an element to a datapack tag file
    private static int addElementToTag(CommandContext<CommandSourceStack> ctx, boolean optional) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("to", ResourceLocation.class);
        var newValue = ctx.getArgument("newElement", ResourceLocation.class);
        addToTag(registry, tagId, optional ? TagEntry.optionalElement(newValue) : TagEntry.element(newValue));
        ctx.getSource().sendSuccess(successPrefix().append(text("Added ")).append(variable(newValue.toString()))
                .append(text(" to ")).append(variable(tagId.toString())).append(text(".")), true);
        return 0;
    }

    // Command: Add a tag to a datapack tag file
    private static int addTagToTag(CommandContext<CommandSourceStack> ctx, boolean optional) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("to", ResourceLocation.class);
        var newTag = ctx.getArgument("newTag", ResourceLocation.class);
        addToTag(registry, tagId, optional ? TagEntry.optionalTag(newTag) : TagEntry.tag(newTag));
        ctx.getSource()
                .sendSuccess(successPrefix().append(text("Added ")).append(variable("#" + newTag)).append(text(" to "))
                        .append(variable(tagId.toString())).append(text(".")), true);
        return 0;
    }

    // Return a list of elements in a resolved tag
    @Nullable
    private static <T> Stream<ResourceLocation> getTagContents(Registry<T> registry, ResourceLocation tagId) {
        var tag = registry.getTag(TagKey.create(registry.key(), tagId));
        return tag.map(holders -> holders.stream().map(holder -> registry.getKey(holder.value()))
                .filter(Objects::nonNull)).orElse(null);
    }

    // Command: show the contents of a given tag
    private static int listTagContents(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var registryId = ctx.getArgument("registry", ResourceLocation.class);
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
        if (registry == null) throw ERROR_UNKNOWN_REGISTRY.create(registryId);
        var tagId = ctx.getArgument("tag", ResourceLocation.class);
        var tagContents = getTagContents(registry, tagId);
        if (tagContents == null) { // valid, just not made yet
            ctx.getSource().sendSystemMessage(CommandUtils.errorPrefix().append(text("No tag exists by the name of "))
                    .append(variable(tagId.toString())));
        } else {
            ctx.getSource().sendSystemMessage(CommandUtils.infoPrefix().append(text("Contents of "))
                    .append(variable(registryId.toString()).withStyle(suggests("/jsst cdd tag " + registryId + " listTags")))
                    .append(text(" tag ")).append(variable(tagId.toString())).append(symbol(":")));
            tagContents.forEach(resloc -> ctx.getSource()
                    .sendSystemMessage(symbol("- ").append(text(resloc.toString()))));
        }
        return 1;
    }

    // Command: show all tags for a given registry, optionally filtered
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
        stream.sorted(Comparator.comparing(TagKey::location)).forEach(key -> ctx.getSource()
                .sendSystemMessage(symbol("- ").append(text(key.location()
                        .toString()).withStyle(suggests("/jsst cdd tag " + registryId + " list " + key.location())))));
        return 1;
    }
}
