package red.jackf.jsst.feature.itemeditor.gui.menus;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.util.Result;
import red.jackf.jsst.util.sgui.CommonLabels;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.Translations;
import red.jackf.jsst.util.sgui.Util;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.menus.Menus;
import red.jackf.jsst.util.sgui.pagination.ListPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComponentMenu extends SimpleGui {
    private final Component initial;
    private final Function<Component, ItemStack> previewBuilder;
    private final Consumer<Result<Component>> callback;
    private final List<Component> parts = new ArrayList<>();
    private final ListPaginator<Component> paginator = ListPaginator.<Component>builder(this)
            .slots(4, 9, 0, 6)
            .list(this.parts)
            .max(50)
            .rowDraw(this::getRow)
            .modifiable(() -> Component.literal("Text"), true)
            .onUpdate(this::redraw)
            .build();

    public ComponentMenu(
            ServerPlayer player,
            Component title,
            Component initial,
            Function<Component, ItemStack> previewBuilder,
            Consumer<Result<Component>> callback) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(title);
        this.initial = initial;
        this.previewBuilder = previewBuilder;
        this.callback = callback;

        this.parts.addAll(this.initial.toFlatList());
    }

    private List<GuiElementInterface> getRow(int index, Component component) {
        return List.of(
                JSSTElementBuilder.ui(Items.NAME_TAG)
                        .setName(component)
                        .leftClick(Component.translatable("jsst.itemEditor.simpleName.changeText"), () -> {
                            Sounds.click(player);
                            Menus.stringBuilder(player)
                                    .title(Component.translatable("jsst.itemEditor.simpleName.changeText"))
                                    .initial(component.getString())
                                    .createAndShow(result -> {
                                        if (result.hasResult()) {
                                            if (result.result().isEmpty()) {
                                                this.parts.remove(index);
                                            } else {
                                                this.parts.set(index, Component.literal(result.result()));
                                            }
                                        }
                                        this.open();
                                    });
                        }).build(),

                JSSTElementBuilder.ui(Items.PAPER)
                        .leftClick(Component.translatable("jsst.itemEditor.simpleName.changeStyle"), () -> {
                            Sounds.click(player);
                            EditorMenus.componentStyle(player, component, opt -> {
                                opt.ifPresent(newComponent -> this.parts.set(index, newComponent));
                                this.open();
                            });
                        }).build()
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
        this.setSlot(Util.slot(1, 1), JSSTElementBuilder.from(this.previewBuilder.apply(build()))
                        .leftClick(Translations.save(), this::complete)
                        .rightClick(Translations.reset(), this::reset));

        this.setSlot(Util.slot(0, 5), CommonLabels.cancel(() -> {
            Sounds.close(player);
            this.callback.accept(Result.empty());
        }));

        for (int row = 0; row < 6; row++) this.setSlot(Util.slot(3, row), CommonLabels.divider());
    }

    private void complete() {
        Sounds.click(player);
        this.callback.accept(Result.of(build()));
    }

    private void reset() {
        Sounds.clear(player);
        this.parts.clear();
        this.parts.addAll(this.initial.toFlatList());
        this.paginator.draw();
        this.redraw();
    }

    @Override
    public void onClose() {
        this.callback.accept(Result.empty());
    }


}
