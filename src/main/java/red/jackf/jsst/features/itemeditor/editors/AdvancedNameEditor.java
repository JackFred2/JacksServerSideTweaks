package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.EditorUtils;
import red.jackf.jsst.features.itemeditor.ItemGuiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;

public class AdvancedNameEditor extends Editor {
    private List<Component> components;
    private int page = 0;
    public AdvancedNameEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> onComplete) {
        super(stack, player, onComplete);
        parseName();
    }

    private void parseName() {
        this.components = stack.hasCustomHoverName() ? stack.getHoverName().toFlatList() : new ArrayList<>();
        this.page = Mth.clamp(this.page, 0, components.size() / 5);
    }

    private void refreshStackName() {
        if (this.components.isEmpty()) {
            this.stack.resetHoverName();
        } else {
            MutableComponent base = Component.literal("");
            this.components.forEach(base::append);
            this.stack.setHoverName(base);
        }
        open();
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(new ItemStack(Items.ANVIL), "Edit Name (Advanced)");
    }

    private void clearName() {
        this.stack.resetHoverName();
        parseName();
        open();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.withHint(stack, "Click to finish"), this::complete));
        elements.put(45, new ItemGuiElement(EditorUtils.makeLabel(Items.WATER_BUCKET, "Clear custom name"), this::clearName)); // TODO: Add confirmation dialogue when modified
        elements.put(47, new ItemGuiElement(EditorUtils.makeLabel(Items.BARRIER, "Cancel"), this::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, new ItemGuiElement(EditorUtils.DIVIDER.copy(), null));

        // Page Buttons
        var maxPage = components.size() / 5;
        if (page > 0)
            elements.put(51, new ItemGuiElement(EditorUtils.makeLabel(Items.RED_CONCRETE, "Previous Page"), () -> {
                this.page = Math.max(0, page - 1);
                open();
            }));
        if (maxPage != 0)
            elements.put(52, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, "Page %s/%s".formatted(page + 1, maxPage + 1)), null));
        if (page < maxPage)
            elements.put(53, new ItemGuiElement(EditorUtils.makeLabel(Items.LIME_CONCRETE, "Next Page"), () -> {
                this.page = Math.min(components.size() / 5, page + 1);
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
            elements.put(startPos, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, text, "Click to edit text"), () -> TextEditor.create(player, text.getString(), newStr -> {
                components.set(finalTextIndex, literal(newStr).setStyle(text.getStyle()));
                refreshStackName();
            })));
            // Edit Style Label
            elements.put(startPos + 1, new ItemGuiElement(EditorUtils.makeLabel(EditorUtils.colourToItem(text.getStyle().getColor()), "Change Style"), () -> StyleEditor.create(player, text, c -> {
                components.remove(finalTextIndex);
                var parts = c.toFlatList();
                for (int i = parts.size() - 1; i >= 0; i--)
                    components.add(finalTextIndex, parts.get(i));
                refreshStackName();
            })));
            // Delete
            elements.put(startPos + 4, new ItemGuiElement(EditorUtils.makeLabel(Items.BARRIER, "Delete"), () -> {
                this.components.remove(finalTextIndex);
                refreshStackName();
            }));
        }

        // New Text Component
        if (page == maxPage)
            elements.put(startPos + 9, new ItemGuiElement(EditorUtils.makeLabel(Items.NETHER_STAR, "Add Component"), () -> {
                this.components.add(literal("Text").withStyle(Style.EMPTY.withItalic(false)));
                refreshStackName();
            }));

        player.openMenu(EditorUtils.make9x6(literal("Editing Name"), elements));
    }
}
