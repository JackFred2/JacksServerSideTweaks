package red.jackf.jsst.impl.utils.sgui.menus;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.utils.Callbacks;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.builder.JSSTElementBuilder;

import java.util.*;
import java.util.function.*;

public class StringInputMenu<T> extends SimpleGuiExt {
    private final String initial;
    private final HintFactory<T> hintFactory;
    private final Function<String, DataResult<T>> parser;
    private final Predicate<T> validator;
    private final OutputFactory<T> outputFunction;
    private final Consumer<Optional<T>> callback;

    private String currentText;

    public StringInputMenu(ServerPlayer player,
                           Component title,
                           String initial,
                           HintFactory<T> hintFactory,
                           Function<String, DataResult<T>> parser,
                           Predicate<T> validator,
                           OutputFactory<T> outputFunction,
                           Consumer<Optional<T>> callback) {
        super(MenuType.ANVIL, player, false);
        this.initial = initial;
        this.hintFactory = hintFactory;
        this.parser = parser;
        this.validator = validator;
        this.outputFunction = outputFunction;
        this.callback = Callbacks.singleUse(callback);
        this.setTitle(title);

        this.currentText = this.initial;

        this.drawStatic();
        this.recieveText(this.initial);
    }

    @Override
    protected void drawStatic() {
        this.setSlot(0, JSSTElementBuilder.from(Items.BARRIER).ui()
                .setName(Component.literal(this.initial))
                .setRarity(Rarity.COMMON)
                .leftClick(Translations.cancel(), () -> {
                    Sounds.UI.close(player);
                    this.cancel();
                })
                .rightClick(Translations.reset(), this::reset));
    }

    public void recieveText(String currentText) {
        this.currentText = currentText;

        DataResult<T> parsed = this.parser.apply(currentText);

        GuiElementInterface hint = this.hintFactory.create(currentText, parsed.result());
        if (hint != null) {
            this.setSlot(1, hint);
        } else {
            this.clearSlot(1);
        }

        this.updateOutput();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        this.sendGui();
        return super.onAnyClick(index, type, action);
    }

    private void updateOutput() {
        DataResult<T> parsed = this.parser.apply(this.currentText);

        if (parsed.isError() || !this.validator.test(parsed.getOrThrow())) {
            this.setSlot(2, JSSTElementBuilder.from(Items.RED_CONCRETE).ui()
                    .setName(Component.literal(this.currentText))
                    .addLoreLine(Component.translatable("jsst.itemEditor.stringInput.invalid")
                            .setStyle(Styles.NEGATIVE)));
        } else {
            JSSTElementBuilder builder = this.outputFunction.create(this.currentText, parsed.getOrThrow());

            this.setSlot(2, builder.leftClick(Translations.confirm(), () -> {
                Sounds.UI.click(player);
                this.complete();
            }));
        }

        this.sendProperty(ScreenProperty.LEVEL_COST, 0);
    }

    private void reset() {
        // right clicking a stack resets the text due to the client picking it up and SGUI cancelling that, placing it in again
        Sounds.UI.reset(this.player);
        this.currentText = this.initial;
        this.updateOutput();
    }

    private void complete() {
        this.parser.apply(this.currentText).ifSuccess(t -> {
            if (this.validator.test(t)) {
                this.callback.accept(Optional.of(t));
            }
        });

        // if it hasn't ran we cancel due to random invalid values
        this.cancel();
    }

    private void cancel() {
        this.callback.accept(Optional.empty());
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    public static class Builder<T> {
        private final ServerPlayer player;
        private final Function<String, DataResult<T>> parser;
        private Component title;
        private String initial = "";
        private Predicate<T> validator = t -> true;
        private HintFactory<T> hintFactory = (raw, t) -> null;
        private final List<Pair<AppendPriority, OutputAppender<T>>> appenders = new ArrayList<>();
        private OutputFactory<T> outputFactory = Builder::defaultOutputFactory;

        protected Builder(ServerPlayer player, Function<String, DataResult<T>> parser) {
            this.player = player;
            this.parser = parser;
        }

        public Builder<T> title(Component title) {
            this.title = title;
            return this;
        }

        public Builder<T> initial(String initial) {
            this.initial = initial;
            return this;
        }

        public Builder<T> validator(Predicate<T> validator) {
            this.validator = validator;
            return this;
        }

        public Builder<T> hint(Component... hintLines) {
            return hints(Arrays.asList(hintLines));
        }

        public Builder<T> hints(List<Component> hintLines) {
            if (hintLines.isEmpty()) {
                return hintElement(null);
            } else {
                return hintFactory((raw, t) -> {
                    var builder = JSSTElementBuilder.from(Items.PAPER).ui().hideDefaultTooltip();
                    hintLines.forEach(builder::addLoreLine);
                    return builder.build();
                });
            }
        }

        public Builder<T> hintElement(@Nullable GuiElementInterface guiElement) {
            return hintFactory((raw, t) -> guiElement);
        }

        public Builder<T> hintFactory(HintFactory<T> hintFactory) {
            this.hintFactory = hintFactory;
            return this;
        }

        public Builder<T> appendOutput(AppendPriority priority, OutputAppender<T> appender) {
            this.appenders.add(Pair.of(priority, appender));
            return this;
        }

        public Builder<T> appendOutput(OutputAppender<T> appender) {
            return appendOutput(AppendPriority.DEFAULT, appender);
        }

        public Builder<T> outputFactory(OutputFactory<T> factory) {
            this.outputFactory = factory;
            this.appenders.clear();
            return this;
        }

        public void start(Consumer<Optional<T>> callback) {
            final OutputFactory<T> initialFactory = this.outputFactory;

            List<OutputAppender<T>> sorted = this.appenders.stream()
                    .sorted(Comparator.comparingInt(pair -> pair.getFirst().ordinal()))
                    .map(Pair::getSecond)
                    .toList();

            OutputFactory<T> factory = (rawText, value) -> {
                var builder = initialFactory.create(rawText, value);
                for (OutputAppender<T> appender : sorted) {
                    appender.append(rawText, value, builder);
                }
                return builder;
            };

            new StringInputMenu<>(player, title, initial, hintFactory, parser, validator, factory, callback).open();
        }

        private static @NotNull <T> JSSTElementBuilder defaultOutputFactory(String text, T parsed) {
            return JSSTElementBuilder.from(Items.LIME_CONCRETE).ui()
                    .setName(Component.literal(text));
        }
    }

    public interface HintFactory<T> {
        @Nullable GuiElementInterface create(String rawText, Optional<T> value);
    }

    public interface OutputFactory<T> {
        @NotNull JSSTElementBuilder create(String rawText, T value);
    }

    public interface OutputAppender<T> {
        void append(String rawText, T value, JSSTElementBuilder builder);
    }

    public enum AppendPriority {
        HIGH,
        DEFAULT,
        LOW
    }
}
