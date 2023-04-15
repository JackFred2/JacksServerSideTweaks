package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;

public class AdvancedComponentMenu {
    private final ServerPlayer player;
    private final int maxComponents;
    private final CancellableCallback<Component> callback;
    private final MutableComponent original;
    private final Function<Component, ItemStack> previewBuilder;
    private final List<Component> components;
    private int page = 0;
    protected AdvancedComponentMenu(ServerPlayer player, Function<Component, ItemStack> previewBuilder, @Nullable Component original, int maxComponents, CancellableCallback<Component> callback) {
        this.player = player;
        this.maxComponents = maxComponents;
        this.callback = callback;
        this.previewBuilder = previewBuilder;
        this.components = original == null ? new ArrayList<>() : original.toFlatList();
        this.original = EditorUtils.mergeComponents(components);
    }

    private void clearName() {
        Sounds.clear(player);
        this.components.clear();
        open();
    }

    public void open() {
        // force lower element count for safety
        if (components.size() > maxComponents) components.subList(maxComponents, components.size()).clear();

        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.withHint(previewBuilder.apply(EditorUtils.mergeComponents(components)), "Click to finish"), () -> callback.accept(EditorUtils.mergeComponents(components))));
        elements.put(45, EditorUtils.clear(this::clearName));
        elements.put(46, EditorUtils.reset(() -> {
            Sounds.clear(player);
            this.components.clear();
            this.components.addAll(original.toFlatList());
            open();
        }));
        elements.put(47, EditorUtils.cancel(callback::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        // Page Buttons
        this.page = Mth.clamp(this.page, 0, components.size() / 5);
        var maxPage = (components.size() / 5) - (components.size() == maxComponents ? 1 : 0);

        EditorUtils.drawPage(elements, components, this.page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, 6, (slot, index) -> {
            var text = components.get(index);
            elements.put(slot, new ItemGuiElement(Labels.create(Items.PAPER).withName(text).withHint("Edit Text").build(), () -> {
                Sounds.interact(player);
                Menus.string(player, text.getString(), newStr -> {
                    Sounds.success(player);
                    components.set(index, literal(newStr).setStyle(text.getStyle()));
                    open();
                });
            }));
            // Edit Style Label
            elements.put(slot + 1, new ItemGuiElement(Labels.create(EditorUtils.colourToItem(text.getStyle().getColor())).withName("Change Style").build(), () -> {
                Sounds.interact(player);
                Menus.style(player, text, c -> {
                    components.remove((int) index);
                    var parts = c.toFlatList();
                    for (int i = parts.size() - 1; i >= 0; i--)
                        components.add(index, parts.get(i));
                    open();
                });
            }));
        }, index -> {
            Sounds.error(player);
            this.components.remove((int) index);
            open();
        }, () -> {
            Sounds.interact(player);
            this.components.add(literal("Text").withStyle(Style.EMPTY.withItalic(false)));
            open();
        });

        player.openMenu(EditorUtils.make9x6(literal("Editing Component"), elements));
    }
}
