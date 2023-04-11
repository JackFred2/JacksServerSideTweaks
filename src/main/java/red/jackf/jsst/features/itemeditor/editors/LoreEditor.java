package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class LoreEditor extends Editor {
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private static final int MAX = 10;
    private final List<Component> lore;


    public LoreEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        this.lore = getLore(stack);
    }

    private static List<Component> getLore(ItemStack stack) {
        var display = stack.getTagElement(ItemStack.TAG_DISPLAY);
        if (display == null || display.getTagType(ItemStack.TAG_LORE) != Tag.TAG_LIST) return new ArrayList<>();
        var lore = display.getList(ItemStack.TAG_LORE, Tag.TAG_LIST);
        var list = new ArrayList<Component>();
        for(int i = 0; i < lore.size(); i++) {
            String line = lore.getString(i);

            try {
                MutableComponent component = Component.Serializer.fromJson(line);
                if (component != null)
                    list.add(ComponentUtils.mergeStyles(component, LORE_STYLE));
            } catch (Exception var19) {
                return new ArrayList<>();
            }
        }
        return list;
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(Items.WRITABLE_BOOK, "Edit Lore");
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.makeLabel(stack, stack.getHoverName(), "Click to finish"), this::complete));
        elements.put(45, EditorUtils.clear(this::cancel));
        elements.put(46, EditorUtils.reset(this::cancel));
        elements.put(47, EditorUtils.cancel(this::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());



        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Lore"), elements));
    }
}
