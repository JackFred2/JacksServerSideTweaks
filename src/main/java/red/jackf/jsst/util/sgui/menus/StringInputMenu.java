package red.jackf.jsst.util.sgui.menus;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.Styles;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StringInputMenu extends SimpleGui {
    private static final int INPUT = 0;
    private static final int HINT = 1;
    private static final int OUTPUT = 2;

    private static final Item INVALID = Items.GRAY_CONCRETE;
    private static final Item VALID = Items.LIME_CONCRETE;

    private final String initial;
    private final Consumer<Result<String>> callback;
    private final Function<String, GuiElementInterface> hint;
    private final Predicate<String> predicate;

    private String text;

    private StringInputMenu(
            ServerPlayer player,
            Component title,
            String initial,
            Function<String, GuiElementInterface> hint,
            Predicate<String> predicate,
            Consumer<Result<String>> callback) {
        super(MenuType.ANVIL, player, false);
        this.hint = hint;
        this.predicate = predicate;
        this.initial = initial;
        this.callback = callback;

        this.text = initial;

        this.setTitle(title);
        this.setSlot(INPUT, JSSTElementBuilder.from(Items.BARRIER)
                                             .setName(Component.literal(initial))
                        .leftClick(Translations.cancel(), this::cancel)
                        .rightClick(Translations.reset(), this::reset));

        this.updateOutput();
    }

    public void onTextUpdate(String text) {
        this.text = text;
        this.setSlot(HINT, hint.apply(text));
        this.updateOutput();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        this.sendGui();
        return super.onAnyClick(index, type, action);
    }

    public void updateOutput() { // update result
        if (this.text.equals(initial)) {
            this.setSlot(OUTPUT, JSSTElementBuilder.from(INVALID)
                                                  .setName(Component.literal(this.text).withStyle(Styles.LABEL))
                                                  .addLoreLine(Component.translatable("jsst.common.noChanges").setStyle(Styles.NEGATIVE)));
        } else if (!this.predicate.test(this.text)) {
            this.setSlot(OUTPUT, JSSTElementBuilder.from(INVALID)
                                                  .setName(Component.literal(this.text).withStyle(Styles.LABEL))
                                                  .addLoreLine(Component.translatable("jsst.common.invalid").setStyle(Styles.NEGATIVE)));
        } else {
            this.setSlot(OUTPUT, JSSTElementBuilder.from(VALID)
                                                  .setName(Component.literal(this.text))
                                                  .leftClick(Translations.confirm(), this::complete));
        }

        // clear level indicator
        this.sendProperty(ScreenProperty.LEVEL_COST, 0);
    }

    @Override
    public void onClose() {
        this.callback.accept(Result.empty());
    }

    private void cancel() {
        Sounds.close(player);
        this.callback.accept(Result.empty());
    }

    private void reset() {
        Sounds.clear(player);
        this.text = initial;
        this.updateOutput();
    }

    private void complete() {
        Sounds.click(player);
        this.callback.accept(Result.of(this.text));
    }

    public static class Builder {
        private final ServerPlayer player;
        private Component title = Component.translatable("jsst.common.input");
        private String initial = "";
        private Predicate<String> predicate = s -> true;
        private Function<String, GuiElementInterface> hint = ignored -> GuiElement.EMPTY;

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

        public Builder predicate(Predicate<String> predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder hint(Function<String, GuiElementInterface> hintBuilder) {
            this.hint = hintBuilder;
            return this;
        }

        public Builder hint(GuiElementInterface hint) {
            this.hint = ignored -> hint;
            return this;
        }

        public Builder hint(GuiElementBuilderInterface<?> hint) {
            GuiElementInterface built = hint.build();
            this.hint = ignored -> built;
            return this;
        }

        public Builder hint(ItemStack hint) {
            return hint(JSSTElementBuilder.from(hint));
        }

        public Builder hint(Component hint) {
            return hint(JSSTElementBuilder.from(Items.PAPER).setName(hint));
        }

        public void createAndShow(Consumer<Result<String>> callback) {
            new StringInputMenu(player, title, initial, hint, predicate, callback).open();
        }
    }
}
