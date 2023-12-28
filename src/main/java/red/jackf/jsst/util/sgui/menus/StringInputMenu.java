package red.jackf.jsst.util.sgui.menus;

import blue.endless.jankson.annotation.Nullable;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StringInputMenu extends SimpleGui {
    private static final int INPUT = 0;
    private static final int HINT = 1;
    private static final int OUTPUT = 2;

    private static final Item INVALID = Items.GRAY_CONCRETE;
    private static final Item VALID = Items.LIME_CONCRETE;

    private final String initial;
    private final Consumer<Optional<String>> onFinish;
    private final Predicate<String> predicate;

    private String text;

    public StringInputMenu(
            ServerPlayer player,
            Component title,
            String initial,
            @Nullable ItemStack hint,
            Predicate<String> predicate,
            Consumer<Optional<String>> onFinish) {
        super(MenuType.ANVIL, player, false);
        this.predicate = predicate;
        this.setTitle(title);
        this.initial = initial;
        this.onFinish = onFinish;

        this.text = initial;

        this.setSlot(INPUT, GuiElementBuilder.from(new ItemStack(Items.BARRIER))
                                             .setName(Component.literal(initial))
                                             .addLoreLine(Hints.leftClick(Translations.cancel()))
                                             .addLoreLine(Hints.rightClick(Translations.reset()))
                                             .setCallback(type -> {
                                                 if (type == ClickType.MOUSE_LEFT) {
                                                     Sounds.close(player);
                                                     this.onFinish.accept(Optional.empty());
                                                 } else if (type == ClickType.MOUSE_RIGHT) {
                                                     Sounds.clear(player);
                                                     this.text = initial;
                                                     this.updateOutput();
                                                 }
                                             }));

        this.setSlot(HINT, GuiElementBuilder.from(hint == null ? ItemStack.EMPTY : hint)
                                            .setCallback(this::sendGui)); // if a player clicks on the output with empty text it all vanishes

        updateOutput();
    }

    public void onTextUpdate(String text) {
        this.text = text;
        this.updateOutput();
    }

    public void updateOutput() { // update result
        if (this.text.equals(initial)) {
            this.setSlot(OUTPUT, GuiElementBuilder.from(new ItemStack(INVALID))
                                                  .setName(Component.literal(this.text).withStyle(Styles.LABEL))
                                                  .addLoreLine(Component.translatable("jsst.common.noChanges").setStyle(Styles.NEGATIVE))
                                                  .setCallback(this::sendGui)); // if a player clicks on the output with empty text it all vanishes
        } else if (!this.predicate.test(this.text)) {
            this.setSlot(OUTPUT, GuiElementBuilder.from(new ItemStack(INVALID))
                                                  .setName(Component.literal(this.text).withStyle(Styles.LABEL))
                                                  .addLoreLine(Component.translatable("jsst.common.invalid").setStyle(Styles.NEGATIVE))
                                                  .setCallback(this::sendGui)); // if a player clicks on the output with empty text it all vanishes
        } else {
            this.setSlot(OUTPUT, GuiElementBuilder.from(new ItemStack(VALID))
                                                  .setName(Component.literal(this.text))
                                                  .addLoreLine(Hints.leftClick(Translations.confirm()))
                                                  .setCallback(Inputs.leftClick(this::onAccept)));
        }
    }

    @Override
    public void onClose() {
        this.onFinish.accept(Optional.empty());
    }

    private void onAccept() {
        Sounds.click(player);
        this.onFinish.accept(Optional.of(this.text));
    }
}
