package red.jackf.jsst.client.impl.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public interface JSSTConfigScreen {
    static Screen create(Screen screen) {
        Collection<ConfigCategory> categories = List.of(
                createBannerWriter(JSSTConfig.INSTANCE),
                createCampfireTimers(JSSTConfig.INSTANCE),
                createItemEditor(JSSTConfig.INSTANCE),
                createItemNudging(JSSTConfig.INSTANCE),
                createMapEditor(JSSTConfig.INSTANCE),
                createPortableCrafting(JSSTConfig.INSTANCE)
        );

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("jsst.title"))
                .categories(categories)
                .save(() -> {
                    JSSTConfig.INSTANCE.save();
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
                                .image(JSST.id("textures/config/banner_writer.png"),320, 240)
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

    private static ConfigCategory createCampfireTimers(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.campfireTimers"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.enabled"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("jsst.config.campfireTimers.description"))
                                .image(JSST.id("textures/config/campfire_timers.png"),320, 240)
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

    private static ConfigCategory createItemNudging(ConfigClassHandler<JSSTConfig> handler) {
        return ConfigCategory.createBuilder()
                .name(translatable("jsst.config.itemNudging"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.itemNudging.shiftUp"))
                        .description(OptionDescription.of(translatable("jsst.config.itemNudging.shiftUp.description")))
                        .binding(handler.defaults().itemNudging.shiftUp,
                                () -> handler.instance().itemNudging.shiftUp,
                                b -> handler.instance().itemNudging.shiftUp = b)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .coloured(true)
                                .yesNoFormatter())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("jsst.config.itemNudging.shiftTowardsPlayer"))
                        .description(OptionDescription.of(translatable("jsst.config.itemNudging.shiftTowardsPlayer.description")))
                        .binding(handler.defaults().itemNudging.shiftTowardsPlayer,
                                () -> handler.instance().itemNudging.shiftTowardsPlayer,
                                b -> handler.instance().itemNudging.shiftTowardsPlayer = b)
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
                                .image(JSST.id("textures/config/map_editor.png"),320, 240)
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
                                .image(JSST.id("textures/config/portable_crafting.png"),320, 240)
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
