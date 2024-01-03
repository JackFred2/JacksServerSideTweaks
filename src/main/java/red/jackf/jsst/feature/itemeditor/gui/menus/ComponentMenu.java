package red.jackf.jsst.feature.itemeditor.gui.menus;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.sgui.*;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComponentMenu extends SimpleGui {
    private final Component initial;
    private final Function<Component, ItemStack> previewBuilder;
    private final Consumer<Component> onResult;
    private final List<Component> parts = new ArrayList<>();
    private final ListPaginator<Component> paginator = ListPaginator.<Component>builder(this)
                                                                    .at(4, 7, 0, 6)
                                                                    .list(this.parts)
                                                                    .max(50)
                                                                    .rowDraw(this::getRow)
                                                                    .modifiable(() -> Component.literal("Text"), false)
                                                                    .onUpdate(this::redraw)
                                                                    .build();

    public ComponentMenu(
            ServerPlayer player,
            Component initial,
            Function<Component, ItemStack> previewBuilder,
            Consumer<Component> onResult) {
        super(MenuType.GENERIC_9x6, player, false);
        this.initial = initial;
        this.previewBuilder = previewBuilder;
        this.onResult = onResult;

        this.parts.addAll(this.initial.toFlatList());
    }

    private List<GuiElementInterface> getRow(int index, Component component) {
        return List.of(
                GuiElementBuilder.from(Items.NAME_TAG.getDefaultInstance())
                                 .setName(component)
                                 .addLoreLine(Hints.leftClick(Component.translatable("jsst.itemEditor.simpleName.changeText")))
                                 .setCallback(Inputs.leftClick(() -> {
                                     Sounds.click(player);
                                     Menus.stringBuilder(player)
                                          .title(Component.translatable("jsst.itemEditor.simpleName.changeText"))
                                          .initial(component.getString())
                                          .createAndShow(opt -> {
                                              opt.ifPresent(s -> {
                                                  if (s.isBlank()) {
                                                      this.parts.remove(index);
                                                  } else {
                                                      this.parts.set(index, Component.literal(s));
                                                  }
                                              });
                                              this.open();
                                          });
                                 })).build(),
                GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                                 .setName(Component.translatable("jsst.itemEditor.simpleName.changeStyle")
                                                   .withStyle(Styles.INPUT_HINT))
                                 .addLoreLine(Hints.leftClick())
                                 .setCallback(Inputs.leftClick(() -> {
                                     Sounds.click(player);
                                     EditorMenus.componentStyle(player, component, opt -> {
                                         opt.ifPresent(newComponent -> this.parts.set(index, newComponent));
                                         this.open();
                                     });
                                 })).build()
        );
    }

    private Component build() {
        if (parts.isEmpty()) return Component.literal("");
        else if (parts.size() == 1) return parts.get(0);

        MutableComponent base = Component.empty();
        parts.forEach(base::append);
        return base;
    }

    @Override
    public void onOpen() {
        this.paginator.draw();

        this.redraw();
    }

    private void redraw() {
        this.setSlot(Util.slot(1, 1), GuiElementBuilder.from(this.previewBuilder.apply(build()))
                                                       .addLoreLine(Hints.leftClick(Translations.save()))
                                                       .addLoreLine(Hints.rightClick(Translations.reset()))
                                                       .setCallback(this::clickPreview));

        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.close();
        }));

        for (int row = 0; row < 6; row++) this.setSlot(Util.slot(3, row), CommonLabels.divider());
    }

    private void clickPreview(ClickType clickType) {
        if (clickType == ClickType.MOUSE_LEFT) {
            Sounds.click(player);
            this.onResult.accept(build());
        } else if (clickType == ClickType.MOUSE_RIGHT) {
            Sounds.clear(player);
            this.parts.clear();
            this.parts.addAll(this.initial.toFlatList());
            this.paginator.draw();
            this.redraw();
        }
    }

    @Override
    public void onClose() {
        this.onResult.accept(initial);
    }




}
