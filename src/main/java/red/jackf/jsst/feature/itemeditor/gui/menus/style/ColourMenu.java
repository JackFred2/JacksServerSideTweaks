package red.jackf.jsst.feature.itemeditor.gui.menus.style;

import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jsst.feature.itemeditor.gui.elements.SwitchButton;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.Map;
import java.util.function.Consumer;

public class ColourMenu extends SimpleGui {
    private final boolean removable;
    private final Consumer<Result<Colour>> callback;

    private Page page = Page.DYES;

    public ColourMenu(
            ServerPlayer player,
            boolean removable,
            Consumer<Result<Colour>> callback) {
        super(MenuType.GENERIC_9x4, player, false);
        this.removable = removable;
        this.callback = callback;

        this.setTitle(Component.translatable("jsst.itemEditor.colour.custom"));
    }

    protected static GuiElementBuilder createFormattingLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = GuiElementBuilder.from(new ItemStack(Items.WRITABLE_BOOK));
        forEachFrame.accept(builder);
        return builder;
    }

    protected static AnimatedGuiElementBuilder createDyeLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = new AnimatedGuiElementBuilder()
                .setInterval(20)
                .setRandom(true);

        for (DyeColor colour : DyeColor.values()) {
            forEachFrame.accept(builder);
            builder.setItem(DyeItem.byColor(colour)).saveItemStack();
        }

        return builder;
    }

    protected static GuiElementBuilder createExtraLabel(Consumer<GuiElementBuilderInterface<?>> forEachFrame) {
        var builder = GuiElementBuilder.from(new ItemStack(Items.CHORUS_FRUIT));
        forEachFrame.accept(builder);
        return builder;
    }

    private void drawColourPage(Map<ItemStack, Colour> colours) {
        var colourSlots = Util.slotTranslator(0, 4, 0, 4);
        Util.fill(this, ItemStack.EMPTY, 0, 4, 0, 4);

        int index = 0;
        for (Map.Entry<ItemStack, Colour> entry : colours.entrySet()) {
            var slot = colourSlots.translate(index++);
            if (slot.isEmpty()) break;
            this.setSlot(slot.getAsInt(), GuiElementBuilder.from(entry.getKey())
                                                           .addLoreLine(Hints.leftClick(Translations.select()))
                                                           .setCallback(Inputs.leftClick(() -> {
                                                               Sounds.click(player);
                                                               this.callback.accept(Result.of(entry.getValue()));
                                                           })));
        }
    }

    @Override
    public void onOpen() {
        this.redraw();
    }

    @Override
    public void onClose() {
        this.callback.accept(Result.empty());
    }

    private void redraw() {
        this.page.pageDraw.accept(this);

        this.setSlot(Util.slot(4, 0), SwitchButton.create(Component.translatable("jsst.itemEditor.colour.page"),
                                                          Page.class,
                                                          this.page,
                                                          newPage -> {
                                                              Sounds.click(player);
                                                              this.page = newPage;
                                                              this.redraw();
                                                          }));

        if (removable) {
            this.setSlot(Util.slot(4, 1), GuiElementBuilder.from(new ItemStack(Items.GUNPOWDER))
                                                           .setName(Component.translatable("jsst.itemEditor.style.removeColour")
                                                                             .withStyle(Styles.INPUT_HINT))
                                                           .addLoreLine(Hints.leftClick())
                                                           .setCallback(Inputs.leftClick(() -> {
                                                               Sounds.clear(player);
                                                               this.callback.accept(Result.of(null));
                                                           })));
        }

        this.setSlot(Util.slot(4, 3), GuiElementBuilder.from(new ItemStack(Items.PAPER))
                                                       .setName(Component.translatable("jsst.itemEditor.colour.custom"))
                                                       .addLoreLine(Hints.leftClick())
                                                       .setCallback(Inputs.leftClick(() -> {
                                                           Sounds.click(player);
                                                           Menus.customColour(player, col -> {
                                                               if (col.isPresent()) {
                                                                   this.callback.accept(Result.of(col.get()));
                                                               } else {
                                                                   this.open();
                                                               }
                                                           });
                                                       })));

        this.setSlot(Util.slot(8, 3), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.callback.accept(Result.empty());
        }));
    }

    private enum Page implements SwitchButton.Labelled {
        DYES(ColourMenu::createDyeLabel, Component.translatable("jsst.itemEditor.colour.dyes"), menu -> menu.drawColourPage(Colours.DYES)),
        FORMATTING(ColourMenu::createFormattingLabel, Component.translatable("jsst.itemEditor.colour.chatFormatting"), menu -> menu.drawColourPage(Colours.CHAT_FORMATS)),
        EXTRA(ColourMenu::createExtraLabel, Component.translatable("jsst.itemEditor.colour.extra"), menu -> menu.drawColourPage(Colours.EXTRA));

        private final SwitchButton.LabelGetter labelSupplier;
        private final Component name;
        private final Consumer<ColourMenu> pageDraw;

        Page(SwitchButton.LabelGetter labelSupplier, Component name, Consumer<ColourMenu> pageDraw) {
            this.labelSupplier = labelSupplier;
            this.name = name;
            this.pageDraw = pageDraw;
        }

        @Override
        public GuiElementBuilderInterface<?> getLabel(Consumer<GuiElementBuilderInterface<?>> applyToEachFrame) {
            return this.labelSupplier.get(applyToEachFrame);
        }

        @Override
        public Component getName() {
            return this.name;
        }
    }
}
