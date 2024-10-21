package red.jackf.jsst.impl.utils.sgui.menus;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.impl.utils.Sounds;
import red.jackf.jsst.impl.utils.sgui.SimpleGuiExt;
import red.jackf.jsst.impl.utils.sgui.Styles;
import red.jackf.jsst.impl.utils.sgui.Translations;
import red.jackf.jsst.impl.utils.sgui.elements.JSSTElementBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StringInputMenu extends SimpleGuiExt {
    private final String initial;
    private final Function<String, @Nullable GuiElementInterface> hintFactory;
    private final Predicate<String> validator;
    private final Consumer<Optional<String>> callback;
    private boolean complete = false;

    private String currentText;

    public StringInputMenu(ServerPlayer player, Component title, String initial, Function<String, @Nullable GuiElementInterface> hintFactory, Predicate<String> validator, Consumer<Optional<String>> callback) {
        super(MenuType.ANVIL, player, false);
        this.initial = initial;
        this.hintFactory = hintFactory;
        this.validator = validator;
        this.callback = callback;

        this.currentText = this.initial;

        this.setTitle(title);

        this.drawStatic();
        this.updateOutput();
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
        GuiElementInterface hint = this.hintFactory.apply(currentText);
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
        if (this.currentText.equals(this.initial)) {
            this.setSlot(2, JSSTElementBuilder.from(Items.RED_CONCRETE).ui()
                    .setName(Component.literal(this.currentText))
                    .addLoreLine(Component.translatable("jsst.itemEditor.stringInput.noChanges").setStyle(Styles.NEGATIVE)));
        } else if (!this.validator.test(this.currentText)) {
            this.setSlot(2, JSSTElementBuilder.from(Items.RED_CONCRETE).ui()
                    .setName(Component.literal(this.currentText))
                    .addLoreLine(Component.translatable("jsst.itemEditor.stringInput.invalid").setStyle(Styles.NEGATIVE)));
        } else {
            this.setSlot(2, JSSTElementBuilder.from(Items.LIME_CONCRETE).ui()
                    .setName(Component.literal(this.currentText))
                    .leftClick(Translations.confirm(), () -> {
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
        if (this.complete) return;
        this.complete = true;
        this.callback.accept(Optional.of(this.currentText));
    }

    private void cancel() {
        if (this.complete) return;
        this.complete = true;
        this.callback.accept(Optional.empty());
    }

    public static class Builder {
        private final ServerPlayer player;
        private Component title;
        private String initial = "";
        private Predicate<String> validator = s -> true;
        private Function<String, @Nullable GuiElementInterface> hintFactory = s -> null;

        protected Builder(ServerPlayer player) {
            this.player = player;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder initial(String initial) {
            this.initial = initial;
            return this;
        }

        public Builder validator(Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        public Builder hint(Component... hintLines) {
            return hint(Arrays.asList(hintLines));
        }

        public Builder hint(List<Component> hintLines) {
            if (hintLines.isEmpty()) {
                this.hintFactory = s -> null;
            } else {
                this.hintFactory = s -> {
                    var builder = JSSTElementBuilder.from(Items.PAPER).ui().hideDefaultTooltip();
                    hintLines.forEach(builder::addLoreLine);
                    return builder.build();
                };
            }
            return this;
        }

        public void start(Consumer<Optional<String>> callback) {
            new StringInputMenu(player, title, initial, hintFactory, validator, callback).open();
        }
    }
}
