package red.jackf.jsst.features.itemeditor.utils.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;

public class AdvancedComponentMenu {
    private final ServerPlayer player;
    private final BlankBehaviour blankBehaviour;
    private final Consumer<Component> callback;
    private final MutableComponent original;
    private final ItemStack previewStack;
    private List<Component> components;
    private int page = 0;
    public AdvancedComponentMenu(ServerPlayer player, ItemStack preview, @Nullable Component start, BlankBehaviour blankBehaviour, Consumer<Component> callback) {
        this.player = player;
        this.blankBehaviour = blankBehaviour;
        this.callback = callback;
        this.previewStack = preview.copy();
        this.components = start == null ? new ArrayList<>() : start.toFlatList();
        this.original = EditorUtils.mergeComponents(components);
    }

    private void clearName() {
        Sounds.clear(player);
        this.components.clear();
        open();
    }

    private ItemStack build() {
        var stack = previewStack.copy();
        if (components.isEmpty() && blankBehaviour == BlankBehaviour.SHOW_STACK_NAME) stack.resetHoverName();
        else stack.setHoverName(EditorUtils.mergeComponents(components));
        return stack;
    }

    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.withHint(build(), "Click to finish"), () -> {
            callback.accept(EditorUtils.mergeComponents(components));
        }));
        elements.put(45, new ItemGuiElement(EditorUtils.makeLabel(Items.WATER_BUCKET, "Clear Custom Name"), this::clearName)); // TODO: Add confirmation dialogue when modified
        elements.put(47, EditorUtils.cancel(() -> {
            callback.accept(original);
        }));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        // Page Buttons
        this.page = Mth.clamp(this.page, 0, components.size() / 5);
        var maxPage = components.size() / 5;
        if (page > 0)
            elements.put(51, new ItemGuiElement(EditorUtils.makeLabel(Items.RED_CONCRETE, "Previous Page"), () -> {
                this.page = Math.max(0, page - 1);
                Sounds.interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
                open();
            }));
        if (maxPage != 0)
            elements.put(52, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, "Page %s/%s".formatted(page + 1, maxPage + 1)), null));
        if (page < maxPage)
            elements.put(53, new ItemGuiElement(EditorUtils.makeLabel(Items.LIME_CONCRETE, "Next Page"), () -> {
                this.page = Math.min(components.size() / 5, page + 1);
                Sounds.interact(player, 1f + ((float) (page + 1) / (maxPage + 1)) / 2);
                open();
            }));

        // Text list
        int startPos = -5;
        int textIndex;
        for (int row = 0; row < 5 && (textIndex = ((page * 5) + row)) < components.size(); row++) {
            startPos = 4 + (row * 9);
            var text = components.get(textIndex);
            int finalTextIndex = textIndex;
            // Edit Text Label
            elements.put(startPos, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, text, "Edit Text"), () -> {
                Sounds.interact(player);
                Menus.simpleText(player, text.getString(), newStr -> {
                    Sounds.success(player);
                    components.set(finalTextIndex, literal(newStr).setStyle(text.getStyle()));
                    open();
                });
            }));
            // Edit Style Label
            elements.put(startPos + 1, new ItemGuiElement(EditorUtils.makeLabel(EditorUtils.colourToItem(text.getStyle().getColor()), "Change Style"), () -> {
                Sounds.interact(player);
                Menus.style(player, text, c -> {
                    components.remove(finalTextIndex);
                    var parts = c.toFlatList();
                    for (int i = parts.size() - 1; i >= 0; i--)
                        components.add(finalTextIndex, parts.get(i));
                    open();
                });
            }));
            // Delete
            elements.put(startPos + 4, new ItemGuiElement(EditorUtils.makeLabel(Items.BARRIER, "Delete"), () -> {
                Sounds.error(player);
                this.components.remove(finalTextIndex);
                open();
            }));
        }

        // New Text Component
        if (page == maxPage)
            elements.put(startPos + 9, new ItemGuiElement(EditorUtils.makeLabel(Items.NETHER_STAR, "Add Component"), () -> {
                Sounds.interact(player);
                this.components.add(literal("Text").withStyle(Style.EMPTY.withItalic(false)));
                open();
            }));

        player.openMenu(EditorUtils.make9x6(literal("Editing Name"), elements));
    }

    public enum BlankBehaviour {
        SHOW_STACK_NAME,
        EMPTY
    }
}
