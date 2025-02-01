package red.jackf.jsst.client.impl.config;

import com.mojang.datafixers.util.Pair;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.feature.itemeditor.ItemEditor;
import red.jackf.jsst.impl.feature.itemeditor.gui.editors.Editor;
import red.jackf.jsst.impl.utils.ServerUtils;
import red.jackf.jsst.impl.utils.TextUtils;

import java.util.*;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public interface JSSTConfigScreen {
    static Screen create(Screen screen) {
        ConfigClassHandler<JSSTConfig> handler = JSSTConfig.INSTANCE;

        StateManager<Float> beaconRangeState = StateManager.createSimple(
                handler.defaults().effectorRanges.beaconRangeModifier,
                () -> handler.instance().effectorRanges.beaconRangeModifier,
                f -> handler.instance().effectorRanges.beaconRangeModifier = f
        );

        Collection<ConfigCategory> categories = List.of(
                createBannerWriter(handler),
                createItemEditor(handler),
                createMapEditor(handler),
                createBeaconEnhancement(handler, beaconRangeState),
                createCampfireTimers(handler),
                createEffectorRanges(handler, beaconRangeState),
                createExtraHighlights(handler),
                createItemNudging(handler),
                createPortableCrafting(handler)
        );

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("jsst.title"))
                .categories(categories)
                .save(() -> {
                    handler.save();
                    onSave();
                })
                .build()
                .generateScreen(screen);
    }

    static void onSave() {
        var localServer = Minecraft.getInstance().getSingleplayerServer();
        if (localServer != null) {
            ServerUtils.refreshCommands(localServer);
        }
    }

    static ConfigCategory createBannerWriter(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.bannerWriter"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.bannerWriter.description"))
                                .text(Component.empty())
                                .text(translatable("jsst.config.bannerWriter.description.credits"))
                                .image(JSST.id("textures/config/banner_writer.png"), 320, 240)
                                .build())
                        .binding(handler.defaults().bannerWriter.enabled,
                                () -> handler.instance().bannerWriter.enabled,
                                b -> handler.instance().bannerWriter.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.requiresOp"))
                        .description(OptionDescription.of(translatable("jsst.config.requiresOp.description")))
                        .binding(handler.defaults().bannerWriter.requiresOp,
                                () -> handler.instance().bannerWriter.requiresOp,
                                i -> handler.instance().bannerWriter.requiresOp = i)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }

    private static ConfigCategory createBeaconEnhancement(ConfigClassHandler<JSSTConfig> handler, StateManager<Float> beaconRangeState) {
        Function<Integer, ListOption<String>> primaryLevelFactory = level -> ListOption.<String>createBuilder()
                .name(translatable("jsst.beaconEnhancement.level", level))
                .binding(handler.defaults().beaconEnhancement.primaryPowers.get(level),
                        () -> handler.instance().beaconEnhancement.primaryPowers.get(level),
                        l -> handler.instance().beaconEnhancement.primaryPowers = handler.instance().beaconEnhancement.primaryPowers.update(level, l))
                .controller(opt -> FormattableStringController.create(opt)
                        .formatter(TextUtils::formatResloc))
                .initial("minecraft:speed_boost")
                .collapsed(true)
                .build();

        Function<Integer, ListOption<String>> secondaryLevelFactory = level -> ListOption.<String>createBuilder()
                .name(translatable("jsst.beaconEnhancement.level", level))
                .binding(handler.defaults().beaconEnhancement.secondaryPowers.get(level),
                        () -> handler.instance().beaconEnhancement.secondaryPowers.get(level),
                        l -> handler.instance().beaconEnhancement.secondaryPowers = handler.instance().beaconEnhancement.secondaryPowers.update(level, l))
                .controller(opt -> FormattableStringController.create(opt)
                        .formatter(TextUtils::formatResloc))
                .initial("minecraft:regeneration")
                .collapsed(true)
                .build();

        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.beaconEnhancement"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.beaconEnhancement.description"))
                                //.image(JSST.id("textures/config/beacon_enhancement.png"),320, 240)
                                .build())
                        .binding(handler.defaults().beaconEnhancement.enabled,
                                () -> handler.instance().beaconEnhancement.enabled,
                                b -> handler.instance().beaconEnhancement.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(translatable("jsst.config.effectorRange.beacon"))
                        .description(modifier -> OptionDescription.createBuilder()
                                .text(translatable("jsst.config.effectorRange.beacon.description"))
                                .text(Component.empty())
                                .text(createBeaconRangeTable(modifier))
                                .build())
                        .stateManager(beaconRangeState)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.5f, 5f)
                                .step(0.01f)
                                .formatValue(value -> literal("%.0f%%".formatted(value * 100))))
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("jsst.config.beaconEnhancement.maxLevel"))
                        .description(OptionDescription.of(translatable("jsst.config.beaconEnhancement.maxLevel.description")))
                        .binding(handler.defaults().beaconEnhancement.maxLevel,
                                () -> handler.instance().beaconEnhancement.maxLevel,
                                i -> handler.instance().beaconEnhancement.maxLevel = i)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(1, 6)
                                .step(1))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(translatable("jsst.config.beaconEnhancement.primaryPowers"))
                        .collapsed(true)
                        .option(LabelOption.create(translatable("jsst.config.beaconEnhancement.primaryPowers.description")))
                        .build())
                .group(primaryLevelFactory.apply(1))
                .group(primaryLevelFactory.apply(2))
                .group(primaryLevelFactory.apply(3))
                .group(primaryLevelFactory.apply(4))
                .group(primaryLevelFactory.apply(5))
                .group(primaryLevelFactory.apply(6))
                .group(OptionGroup.createBuilder()
                        .name(translatable("jsst.config.beaconEnhancement.secondaryPowers"))
                        .option(Option.<Boolean>createBuilder()
                                .name(translatable("jsst.config.beaconEnhancement.enableSecondPower"))
                                .description(OptionDescription.createBuilder()
                                        .text(translatable("jsst.config.beaconEnhancement.enableSecondPower.description"))
                                        .build())
                                .binding(handler.defaults().beaconEnhancement.enableSecondPower,
                                        () -> handler.instance().beaconEnhancement.enableSecondPower,
                                        b -> handler.instance().beaconEnhancement.enableSecondPower = b)
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .coloured(true)
                                        .yesNoFormatter())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(translatable("jsst.config.beaconEnhancement.secondPowerMinLevel"))
                                .description(OptionDescription.of(translatable("jsst.config.beaconEnhancement.secondPowerMinLevel.description")))
                                .binding(handler.defaults().beaconEnhancement.secondPowerMinLevel,
                                        () -> handler.instance().beaconEnhancement.secondPowerMinLevel,
                                        i -> handler.instance().beaconEnhancement.secondPowerMinLevel = i)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(1, 6)
                                        .step(1))
                                .build())
                        .build())
                .group(secondaryLevelFactory.apply(1))
                .group(secondaryLevelFactory.apply(2))
                .group(secondaryLevelFactory.apply(3))
                .group(secondaryLevelFactory.apply(4))
                .group(secondaryLevelFactory.apply(5))
                .group(secondaryLevelFactory.apply(6))
                .build();
    }

    private static ConfigCategory createCampfireTimers(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.campfireTimers"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.campfireTimers.description"))
                                .image(JSST.id("textures/config/campfire_timers.png"), 320, 240)
                                .build())
                        .binding(handler.defaults().campfireTimers.enabled,
                                () -> handler.instance().campfireTimers.enabled,
                                b -> handler.instance().campfireTimers.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }

    private static ConfigCategory createExtraHighlights(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.extraHighlights"))
                .group(OptionGroup.createBuilder()
                        .name(translatable("jsst.config.extraHighlights.treeLogs"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.extraHighlights.treeLogs.description"))
                                .image(JSST.id("textures/config/extra_highlights_tree_logs.png"), 320, 240)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(translatable("jsst.config.enabled"))
                                .binding(handler.defaults().extraHighlights.treeLogsEnabled,
                                        () -> handler.instance().extraHighlights.treeLogsEnabled,
                                        b -> handler.instance().extraHighlights.treeLogsEnabled = b)
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .coloured(true)
                                        .yesNoFormatter())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(translatable("jsst.config.extraHighlights.highlightTime"))
                                .description(OptionDescription.of(translatable("jsst.config.extraHighlights.highlightTime.description")))
                                .binding(handler.defaults().extraHighlights.highlightTime,
                                        () -> handler.instance().extraHighlights.highlightTime,
                                        i -> handler.instance().extraHighlights.highlightTime = i)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(20 * 5, 20 * 15)
                                        .step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(translatable("jsst.config.extraHighlights.treeLogsRange"))
                                .description(OptionDescription.of(translatable("jsst.config.extraHighlights.treeLogsRange.description")))
                                .binding(handler.defaults().extraHighlights.treeLogsRange,
                                        () -> handler.instance().extraHighlights.treeLogsRange,
                                        i -> handler.instance().extraHighlights.treeLogsRange = i)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(6, 16)
                                        .step(1))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(translatable("jsst.config.extraHighlights.logsTag"))
                                .description(OptionDescription.of(translatable("jsst.config.extraHighlights.logsTag.description")))
                                .binding(handler.defaults().extraHighlights.logsTag,
                                        () -> handler.instance().extraHighlights.logsTag,
                                        s -> handler.instance().extraHighlights.logsTag = s)
                                .controller(opt -> FormattableStringController.create(opt)
                                        .formatter(TextUtils::formatResloc))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(translatable("jsst.config.extraHighlights.axesTag"))
                                .description(OptionDescription.of(translatable("jsst.config.extraHighlights.axesTag.description")))
                                .binding(handler.defaults().extraHighlights.axesTag,
                                        () -> handler.instance().extraHighlights.axesTag,
                                        s -> handler.instance().extraHighlights.axesTag = s)
                                .controller(opt -> FormattableStringController.create(opt)
                                        .formatter(TextUtils::formatResloc))
                                .build())
                        .build()
                ).option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.extraHighlights.sugarcaneEnabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.extraHighlights.sugarcaneEnabled.description"))
                                .image(JSST.id("textures/config/extra_highlights_sugarcane.png"), 320, 240)
                                .build())
                        .binding(handler.defaults().extraHighlights.sugarcaneEnabled,
                                () -> handler.instance().extraHighlights.sugarcaneEnabled,
                                b -> handler.instance().extraHighlights.sugarcaneEnabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build()).build();
    }

    static ConfigCategory createItemEditor(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.itemEditor"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.itemEditor.description"))
                                //.image(JSST.id("textures/config/item_editor.png"),320, 240)
                                .build())
                        .binding(handler.defaults().itemEditor.enabled,
                                () -> handler.instance().itemEditor.enabled,
                                b -> handler.instance().itemEditor.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.itemEditor.nonOpsCanUseCosmeticMode"))
                        .description(OptionDescription.of(translatable("jsst.config.itemEditor.nonOpsCanUseCosmeticMode.description")))
                        .binding(handler.defaults().itemEditor.nonOpsCanUseCosmeticMode,
                                () -> handler.instance().itemEditor.nonOpsCanUseCosmeticMode,
                                i -> handler.instance().itemEditor.nonOpsCanUseCosmeticMode = i)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .group(OptionGroup.createBuilder()
                        .collapsed(true)
                        .name(translatable("jsst.config.itemEditor.disabledEditors"))
                        .options(ItemEditor.EDITORS.stream()
                                .sorted(Comparator.comparing(Editor.Type::getId))
                                .map(type -> Option.<Boolean>createBuilder()
                                        .name(literal(type.getId().toString()))
                                        .binding(true,
                                                () -> !handler.instance().itemEditor.disabledEditors.contains(type.getId()),
                                                b -> {
                                                    if (b) {
                                                        handler.instance().itemEditor.disabledEditors.remove(type.getId());
                                                    } else {
                                                        handler.instance().itemEditor.disabledEditors.add(type.getId());
                                                    }
                                                })
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .coloured(true)
                                                .onOffFormatter())
                                        .build())
                                .toList())
                        .build())
                .build();
    }

    private static Collection<Component> createBeaconRangeTable(float rangeModifier) {
        List<Component> list = new ArrayList<>();

        for (int level = 1; level <= 6; level++) {
            int range = 10 * (1 + level);

            list.add(translatable("jsst.config.effectorRange.beacon.description.example", level, (int) (range * rangeModifier)));
        }

        return list;
    }

    private static Collection<Component> createConduitRangeTable(float rangeModifier) {
        List<Component> list = new ArrayList<>();
        List<Pair<Integer, Double>> ranges = List.of(
                Pair.of(16, 32.0),
                Pair.of(21, 48.0),
                Pair.of(28, 64.0),
                Pair.of(35, 80.0),
                Pair.of(42, 96.0)
        );

        for (var range : ranges) {
            list.add(translatable("jsst.config.effectorRange.conduit.description.example", range.getFirst(), (int) (range.getSecond() * rangeModifier)));
        }

        return list;
    }

    private static ConfigCategory createEffectorRanges(ConfigClassHandler<JSSTConfig> handler, StateManager<Float> beaconRangeState) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.effectorRange"))
                .option(Option.<Float>createBuilder()
                        .name(translatable("jsst.config.effectorRange.beacon"))
                        .description(modifier -> OptionDescription.createBuilder()
                                .text(translatable("jsst.config.effectorRange.beacon.description"))
                                .text(Component.empty())
                                .text(createBeaconRangeTable(modifier))
                                .build())
                        .stateManager(beaconRangeState)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.5f, 5f)
                                .step(0.01f)
                                .formatValue(value -> literal("%.0f%%".formatted(value * 100))))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(translatable("jsst.config.effectorRange.conduit"))
                        .description(modifier -> OptionDescription.createBuilder()
                                .text(translatable("jsst.config.effectorRange.conduit.description"))
                                .text(Component.empty())
                                .text(createConduitRangeTable(modifier))
                                .build())
                        .binding(handler.defaults().effectorRanges.conduitRangeModifier,
                                () -> handler.instance().effectorRanges.conduitRangeModifier,
                                f -> handler.instance().effectorRanges.conduitRangeModifier = f)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.5f, 5f)
                                .step(0.01f)
                                .formatValue(value -> literal("%.0f%%".formatted(value * 100))))
                        .build())
                .build();
    }

    private static ConfigCategory createItemNudging(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.itemNudging"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.itemNudging.shiftUp"))
                        .description(OptionDescription.of(translatable("jsst.config.itemNudging.shiftItemsUp.description")))
                        .binding(handler.defaults().itemNudging.shiftItemsUp,
                                () -> handler.instance().itemNudging.shiftItemsUp,
                                b -> handler.instance().itemNudging.shiftItemsUp = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.itemNudging.shiftItemsTowardsPlayer"))
                        .description(OptionDescription.of(translatable("jsst.config.itemNudging.shiftItemsTowardsPlayer.description")))
                        .binding(handler.defaults().itemNudging.shiftItemsTowardsPlayer,
                                () -> handler.instance().itemNudging.shiftItemsTowardsPlayer,
                                b -> handler.instance().itemNudging.shiftItemsTowardsPlayer = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
            .build();
    }

    private static ConfigCategory createMapEditor(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.mapEditor"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.mapEditor.description"))
                                .image(JSST.id("textures/config/map_editor.png"), 320, 240)
                                .build())
                        .binding(handler.defaults().mapEditor.enabled,
                                () -> handler.instance().mapEditor.enabled,
                                b -> handler.instance().mapEditor.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.requiresOp"))
                        .description(OptionDescription.of(translatable("jsst.config.requiresOp.description")))
                        .binding(handler.defaults().mapEditor.requiresOp,
                                () -> handler.instance().mapEditor.requiresOp,
                                i -> handler.instance().mapEditor.requiresOp = i)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<String>createBuilder()
                        .name(translatable("jsst.config.mapEditor.tool"))
                        .description(OptionDescription.of(translatable("jsst.config.mapEditor.tool.description"),
                                Component.empty(),
                                translatable("jsst.config.idOrTagDescription")))
                        .binding(handler.defaults().mapEditor.tool,
                                () -> handler.instance().mapEditor.tool,
                                s -> handler.instance().mapEditor.tool = s)
                        .controller(opt -> FormattableStringController.create(opt)
                                .formatter(TextUtils::formatReslocOrTag))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.mapEditor.disableSerialization"))
                        .description(OptionDescription.of(translatable("jsst.config.mapEditor.disableSerialization.description")))
                        .binding(handler.defaults().mapEditor.disableSerialization,
                                () -> handler.instance().mapEditor.disableSerialization,
                                s -> handler.instance().mapEditor.disableSerialization = s)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }

    private static ConfigCategory createPortableCrafting(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.portableCrafting"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.portableCrafting.description"))
                                .image(JSST.id("textures/config/portable_crafting.png"), 320, 240)
                                .build())
                        .binding(handler.defaults().portableCrafting.enabled,
                                () -> handler.instance().portableCrafting.enabled,
                                b -> handler.instance().portableCrafting.enabled = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<String>createBuilder()
                        .name(translatable("jsst.config.portableCrafting.itemIdOrTag"))
                        .description(OptionDescription.of(translatable("jsst.config.portableCrafting.itemIdOrTag.description"),
                                Component.empty(),
                                translatable("jsst.config.idOrTagDescription")))
                        .binding(handler.defaults().portableCrafting.itemIdOrTag,
                                () -> handler.instance().portableCrafting.itemIdOrTag,
                                s -> handler.instance().portableCrafting.itemIdOrTag = s)
                        .controller(opt -> FormattableStringController.create(opt)
                                .formatter(TextUtils::formatReslocOrTag))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.portableCrafting.requiresSneak"))
                        .binding(handler.defaults().portableCrafting.requiresSneak,
                                () -> handler.instance().portableCrafting.requiresSneak,
                                b -> handler.instance().portableCrafting.requiresSneak = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .build();
    }
}
