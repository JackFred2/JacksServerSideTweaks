package red.jackf.jsst.client.impl.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.jsst.impl.JSST;
import red.jackf.jsst.impl.config.JSSTConfig;
import red.jackf.jsst.impl.utils.StringUtils;

import java.util.Collection;
import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public interface JSSTConfigScreen {
    static Screen create(Screen screen) {
        Collection<ConfigCategory> categories = List.of(
                createPortableCrafting(JSSTConfig.INSTANCE),
                createCampfireTimers(JSSTConfig.INSTANCE),
                createItemNudging(JSSTConfig.INSTANCE)
        );

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("jsst.title"))
                .categories(categories)
                .build()
                .generateScreen(screen);
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
                        .description(OptionDescription.of(translatable("jsst.config.portableCrafting.itemIdOrTag.description")))
                        .binding(handler.defaults().portableCrafting.itemIdOrTag,
                                () -> handler.instance().portableCrafting.itemIdOrTag,
                                s -> handler.instance().portableCrafting.itemIdOrTag = s)
                        .controller(opt -> FormattableStringController.create(opt)
                                .formatter(StringUtils::formatReslocOrTag))
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
}
