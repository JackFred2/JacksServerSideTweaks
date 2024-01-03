package red.jackf.jsst.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import red.jackf.jsst.JSST;
import red.jackf.jsst.config.JSSTConfig;
import red.jackf.jsst.feature.anvilenhancement.AnvilEnhancement;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

@SuppressWarnings({"SameParameterValue"})
public class CommandConfig {
    private static final String BASE_WIKI_URL = "https://github.com/JackFred2/JacksServerSideTweaks/wiki/";

    private CommandConfig() {}

    private static String makeWikiLink(String basePage, String optionName) {
        return BASE_WIKI_URL + basePage + "#" + optionName.toLowerCase(Locale.ROOT).replace(".", "");
    }

    private static JSSTConfig getConfig() {
        return JSST.CONFIG.instance();
    }

    private static void verifySafeAndLoad() {
        getConfig().validate();
        JSST.CONFIG.save();
        JSST.CONFIG.load();
    }

    private static Component makeHover(String name, String fullName, String baseWikiPage) {
        return Formatting.variable(literal(name).withStyle(Style.EMPTY.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                               Component.empty()
                                        .append(Formatting.variable("$." + fullName))
                                        .append(CommonComponents.NEW_LINE)
                                        .append(translatable("jsst.command.config.open_wiki")))
        ).withClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL, makeWikiLink(baseWikiPage, fullName))
        )));
    }

    //////////////
    // BUILDERS //
    //////////////
    private static LiteralArgumentBuilder<CommandSourceStack> makeBoolean(
            String name,
            String fullName,
            String baseWikiPage,
            Function<JSSTConfig, Boolean> get,
            BiConsumer<JSSTConfig, Boolean> set) {
        var root = Commands.literal(name)
                           .executes(ctx -> {
                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                       translatable("jsst.command.config.check",
                                                    makeHover(name, fullName, baseWikiPage),
                                                    Formatting.bool(get.apply(getConfig())))
                               ), false);

                               return 1;
                           });

        root.then(Commands.literal("true")
                          .executes(ctx -> {
                                        if (get.apply(getConfig())) {
                                            ctx.getSource().sendFailure(Formatting.infoLine(
                                                    translatable("jsst.command.config.unchanged",
                                                                 makeHover(name, fullName, baseWikiPage),
                                                                 Formatting.bool(true))
                                            ));

                                            return 0;
                                        } else {
                                            set.accept(getConfig(), true);
                                            verifySafeAndLoad();
                                            ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                    translatable("jsst.command.config.change",
                                                                 makeHover(name, fullName, baseWikiPage),
                                                                 Formatting.bool(false),
                                                                 Formatting.bool(true))
                                            ), true);

                                            return 1;
                                        }
                                    }
                          ));

        root.then(Commands.literal("false")
                          .executes(ctx -> {
                                        if (!get.apply(getConfig())) {
                                            ctx.getSource().sendFailure(Formatting.infoLine(
                                                    translatable("jsst.command.config.unchanged",
                                                                 makeHover(name, fullName, baseWikiPage),
                                                                 Formatting.bool(false))
                                            ));

                                            return 0;
                                        } else {
                                            set.accept(getConfig(), false);
                                            verifySafeAndLoad();
                                            ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                    translatable("jsst.command.config.change",
                                                                 makeHover(name, fullName, baseWikiPage),
                                                                 Formatting.bool(true),
                                                                 Formatting.bool(false))
                                            ), true);

                                            return 1;
                                        }
                                    }
                          ));

        return root;
    }

    private static <E extends Enum<E>> LiteralArgumentBuilder<CommandSourceStack> makeEnum(
            String name,
            String fullName,
            String baseWikiPage,
            Class<E> enumClass,
            Function<JSSTConfig, E> get,
            BiConsumer<JSSTConfig, E> set) {
        var node = Commands.literal(name)
                           .executes(ctx -> {
                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                       translatable("jsst.command.config.check",
                                                    makeHover(name, fullName, baseWikiPage),
                                                    Formatting.string(get.apply(getConfig()).name()))
                               ), false);

                               return 1;
                           });

        for (E constant : enumClass.getEnumConstants()) {
            node.then(Commands.literal(constant.name())
                              .executes(ctx -> {
                                  var old = get.apply(getConfig());
                                  if (old == constant) {
                                      ctx.getSource().sendFailure(Formatting.infoLine(
                                              translatable("jsst.command.config.unchanged",
                                                           makeHover(name, fullName, baseWikiPage),
                                                           Formatting.string(constant.name()))
                                      ));

                                      return 0;
                                  } else {
                                      set.accept(getConfig(), constant);
                                      verifySafeAndLoad();
                                      ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                              translatable("jsst.command.config.change",
                                                           makeHover(name, fullName, baseWikiPage),
                                                           Formatting.string(old.name()),
                                                           Formatting.string(constant.name()))
                                      ), true);

                                      return 1;
                                  }
                              })
            );
        }

        return node;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeIntRange(
            String name,
            String fullName,
            String baseWikiPage,
            int min,
            int max,
            Function<JSSTConfig, Integer> get,
            BiConsumer<JSSTConfig, Integer> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                   translatable("jsst.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.integer(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, IntegerArgumentType.integer(min, max))
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = IntegerArgumentType.getInteger(ctx, name);
                                           if (old == newValue) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("jsst.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.integer(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();
                                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                       translatable("jsst.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.integer(old),
                                                                    Formatting.integer(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeDoubleRange(
            String name,
            String fullName,
            String baseWikiPage,
            double min,
            double max,
            Function<JSSTConfig, Double> get,
            BiConsumer<JSSTConfig, Double> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                   translatable("jsst.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.ddouble(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, DoubleArgumentType.doubleArg(min, max))
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = DoubleArgumentType.getDouble(ctx, name);
                                           if (old == newValue) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("jsst.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.ddouble(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();
                                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                       translatable("jsst.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.ddouble(old),
                                                                    Formatting.ddouble(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }
/*
    private static LiteralArgumentBuilder<CommandSourceStack> makeWord(String name,
                                                                       String fullName,
                                                                       String baseWikiPage,
                                                                       Function<JSSTConfig, String> get,
                                                                       BiConsumer<JSSTConfig, String> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                   translatable("eyespy.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.string(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, StringArgumentType.word())
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = StringArgumentType.getString(ctx, name);
                                           if (old.equals(newValue)) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("eyespy.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.string(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();

                                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                       translatable("eyespy.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.string(old),
                                                                    Formatting.string(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }*/

    ///////////
    // NODES //
    ///////////

    public static LiteralArgumentBuilder<CommandSourceStack> createCommandNode(CommandBuildContext context) {
        var root = Commands.literal("config");

        root.then(makeItemEditorNode());
        root.then(makePortableCraftingNode());
        root.then(makeWorldContainerNamesNode());
        root.then(makeBeaconEnhancementNode(context));
        root.then(makeAnvilEnhancementNode());
        root.then(makeQolNode());

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeItemEditorNode() {
        var root = Commands.literal("itemEditor");

        root.then(makeBoolean("cosmeticOnlyModeAvailable",
                              "itemEditor.cosmeticOnlyModeAvailable",
                              WikiPage.ITEM_EDITOR,
                              config -> config.itemEditor.cosmeticOnlyModeAvailable,
                              (config, newVal) -> config.itemEditor.cosmeticOnlyModeAvailable = newVal));

        root.then(makeBoolean("dedicatedCommand",
                              "itemEditor.dedicatedCommand",
                              WikiPage.ITEM_EDITOR,
                              config -> config.itemEditor.dedicatedCommand,
                              (config, newVal) -> config.itemEditor.dedicatedCommand = newVal));

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makePortableCraftingNode() {
        var root = Commands.literal("portableCrafting");

        root.then(makeBoolean("enabled",
                              "portableCrafting.enabled",
                              WikiPage.PORTABLE_CRAFTING,
                              config -> config.portableCrafting.enabled,
                              (config, newVal) -> config.portableCrafting.enabled = newVal));

        root.then(makeBoolean("requireSneak",
                              "portableCrafting.requireSneak",
                              WikiPage.PORTABLE_CRAFTING,
                              config -> config.portableCrafting.requireSneak,
                              (config, newVal) -> config.portableCrafting.requireSneak = newVal));

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeWorldContainerNamesNode() {
        var root = Commands.literal("worldContainerNames");

        root.then(makeBoolean("enabled",
                              "worldContainerNames.enabled",
                              WikiPage.WORLD_CONTAINER_NAMES,
                              config -> config.worldContainerNames.enabled,
                              (config, newVal) -> config.worldContainerNames.enabled = newVal));

        root.then(makeDoubleRange("viewRange",
                                  "worldContainerNames.viewRange",
                                  WikiPage.WORLD_CONTAINER_NAMES,
                                  4, 16,
                                  config -> config.worldContainerNames.viewRange,
                                  (config, newVal) -> config.worldContainerNames.viewRange = newVal));

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeBeaconEnhancementNode(CommandBuildContext context) {
        var root = Commands.literal("beaconEnhancement");

        root.then(makeBoolean("enabled",
                              "beaconEnhancement.enabled",
                              WikiPage.BEACON_ENHANCEMENT,
                              config -> config.beaconEnhancement.enabled,
                              (config, newVal) -> config.beaconEnhancement.enabled = newVal));

        root.then(makeDoubleRange("rangeMultiplier",
                                  "beaconEnhancement.rangeMultiplier",
                                  WikiPage.BEACON_ENHANCEMENT,
                                  0.5, 8,
                                  config -> config.beaconEnhancement.rangeMultiplier,
                                  (config, newVal) -> config.beaconEnhancement.rangeMultiplier = newVal));

        root.then(makeIntRange("maxBeaconLevel",
                               "beaconEnhancement.maxBeaconLevel",
                               WikiPage.BEACON_ENHANCEMENT,
                               4, 6,
                               config -> config.beaconEnhancement.maxBeaconLevel,
                               (config, newVal) -> config.beaconEnhancement.maxBeaconLevel = newVal));

        root.then(makeBeaconEnhancementPowersNode(context));

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeBeaconEnhancementPowersNode(CommandBuildContext context) {
        var root = Commands.literal("powerSet");
        final String fullName = "beaconEnhancement.powerSet";

        root.executes(ctx -> {
            var powerSet = getConfig().beaconEnhancement.powers;

            ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                    Component.empty()
                             .append(makeHover("powerSet", fullName, WikiPage.BEACON_ENHANCEMENT))
                             .append(literal(":"))
            ), false);

            for (int level = 1; level <= 6; level++) {
                int finalLevel = level;
                ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                        Component.literal("  ")
                                 .append(translatable("jsst.command.config.powerListHeader", finalLevel))
                                 .append(":")
                ), false);

                var powers = powerSet.getAtLevel(level);

                if (powers.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                            Component.literal("  ").append(translatable("jsst.command.config.list.empty"))
                    ), false);
                } else {
                    for (MobEffect power : powers) {
                        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(power);
                        ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                Component.literal("  ")
                                         .append(Formatting.listItem(Formatting.variable(String.valueOf(id))))
                        ), false);
                    }
                }
            }

            return 1;
        });

        for (int level = 1; level <= 6; level++) {
            var levelNode = Commands.literal(String.valueOf(level));
            final String name = "powerSet." + level;

            int finalLevel = level;
            levelNode.executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                        Component.empty()
                                 .append(translatable("jsst.command.config.powerListHeader", finalLevel))
                                 .append(":")
                ), false);

                var powers = getConfig().beaconEnhancement.powers.getAtLevel(finalLevel);

                if (powers.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                            Component.empty().append(translatable("jsst.command.config.list.empty"))
                    ), false);
                } else {
                    for (MobEffect power : powers) {
                        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(power);
                        ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                Component.empty().append(Formatting.listItem(Formatting.variable(String.valueOf(id))))
                        ), false);
                    }
                }

                return 1;
            });

            levelNode.then(Commands.literal("add")
                                   .then(Commands.argument("effect", ResourceArgument.resource(context, Registries.MOB_EFFECT))
                                                 .suggests((ctx, builder) -> {
                                                     var existing = getConfig().beaconEnhancement.powers.getAtLevel(finalLevel);
                                                     return SharedSuggestionProvider.suggestResource(BuiltInRegistries.MOB_EFFECT.entrySet().stream()
                                                                                                                                 .filter(entry -> !existing.contains(entry.getValue()))
                                                                                                                                 .map(e -> e.getKey().location()),
                                                                                                     builder);
                                                 })
                                                 .executes(ctx -> {
                                                     var effect = ResourceArgument.getMobEffect(ctx, "effect");
                                                     var powers = getConfig().beaconEnhancement.powers;
                                                     var atThisLevel = powers.getAtLevel(finalLevel);
                                                     if (atThisLevel.contains(effect.value())) {
                                                         ctx.getSource().sendFailure(Formatting.errorLine(
                                                                 translatable("jsst.command.config.list.alreadyContains",
                                                                              makeHover(name,
                                                                                        fullName,
                                                                                        WikiPage.BEACON_ENHANCEMENT),
                                                                              Formatting.variable(effect.key().location().toString()))
                                                         ));
                                                         return 0;
                                                     } else {
                                                         powers.addPower(finalLevel, effect.value());
                                                         verifySafeAndLoad();
                                                         ctx.getSource().sendSuccess(() -> Formatting.successLine(
                                                                 translatable("jsst.command.config.list.added",
                                                                              makeHover(name,
                                                                                        fullName,
                                                                                        WikiPage.BEACON_ENHANCEMENT),
                                                                              Formatting.variable(effect.key().location().toString()))
                                                         ), true);
                                                         return 1;
                                                     }
                                                 })));

            levelNode.then(Commands.literal("remove")
                                   .then(Commands.argument("effect", ResourceArgument.resource(context, Registries.MOB_EFFECT))
                                                 .suggests((ctx, builder) -> {
                                                     var existing = getConfig().beaconEnhancement.powers.getAtLevel(finalLevel);
                                                     return SharedSuggestionProvider.suggestResource(existing.stream().map(BuiltInRegistries.MOB_EFFECT::getKey),
                                                                                                     builder);
                                                 })
                                                 .executes(ctx -> {
                                                     var effect = ResourceArgument.getMobEffect(ctx, "effect");
                                                     var powers = getConfig().beaconEnhancement.powers;
                                                     var atThisLevel = powers.getAtLevel(finalLevel);
                                                     if (!atThisLevel.contains(effect.value())) {
                                                         ctx.getSource().sendFailure(Formatting.errorLine(
                                                                 translatable("jsst.command.config.list.doesNotContain",
                                                                              makeHover(name,
                                                                                        fullName,
                                                                                        WikiPage.BEACON_ENHANCEMENT),
                                                                              Formatting.variable(effect.key().location().toString()))
                                                         ));
                                                         return 0;
                                                     } else {
                                                         powers.removePower(finalLevel, effect.value());
                                                         verifySafeAndLoad();
                                                         ctx.getSource().sendSuccess(() -> Formatting.successLine(
                                                                 translatable("jsst.command.config.list.removed",
                                                                              makeHover(name,
                                                                                        fullName,
                                                                                        WikiPage.BEACON_ENHANCEMENT),
                                                                              Formatting.variable(effect.key().location().toString()))
                                                         ), true);
                                                         return 1;
                                                     }
                                                 })));

            root.then(levelNode);
        }

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeAnvilEnhancementNode() {
        var root = Commands.literal("anvilEnhancement");

        root.then(makeEnum("renameCost",
                           "anvilEnhancement.renameCost",
                           WikiPage.ANVIL_ENHANCEMENT,
                           AnvilEnhancement.RenameCost.class,
                           config -> config.anvilEnhancement.renameCost,
                           (config, newVal) -> config.anvilEnhancement.renameCost = newVal));

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeQolNode() {
        var root = Commands.literal("qol");

        root.then(makeBoolean("doMinedItemsShiftUp",
                              "qol.doMinedItemsShiftUp",
                              WikiPage.QOL,
                              config -> config.qol.doMinedItemsShiftUp,
                              (config, newVal) -> config.qol.doMinedItemsShiftUp = newVal));

        root.then(makeBoolean("doMinedItemsShiftTowardsPlayer",
                              "qol.doMinedItemsShiftTowardsPlayer",
                              WikiPage.QOL,
                              config -> config.qol.doMinedItemsShiftTowardsPlayer,
                              (config, newVal) -> config.qol.doMinedItemsShiftTowardsPlayer = newVal));

        return root;
    }

    private interface WikiPage {
        String PORTABLE_CRAFTING = "Portable-Crafting";
        String BEACON_ENHANCEMENT = "Beacon-Enhancement";
        String WORLD_CONTAINER_NAMES = "World-Container-Names";
        String ITEM_EDITOR = "Item-Editor";
        String QOL = "Quality-Of-Life";
        String ANVIL_ENHANCEMENT = "Anvil-Enhancement";
    }
}
