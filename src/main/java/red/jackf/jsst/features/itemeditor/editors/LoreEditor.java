package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.EditorUtils;
import red.jackf.jsst.features.itemeditor.utils.ItemGuiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;

public class LoreEditor extends Editor {
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private static final int MAX_LORE = 15;
    private List<Component> lore;
    private int page = 0;


    public LoreEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
        this.lore = getLore(stack);
    }

    private static List<Component> getLore(ItemStack stack) {
        var display = stack.getTagElement(ItemStack.TAG_DISPLAY);
        if (display == null || display.getTagType(ItemStack.TAG_LORE) != Tag.TAG_LIST) return new ArrayList<>();
        var lore = display.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
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

    private static ListTag toTag(List<Component> lore) {
        var list = new ListTag();
        for (Component component : lore) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(component)));
        }
        return list;
    }

    private static ItemStack setLore(ItemStack stack, List<Component> lore) {
        var display = stack.getTagElement(ItemStack.TAG_DISPLAY);
        if (lore.size() == 0 && display != null && display.contains(ItemStack.TAG_LORE)) {
            display.remove(ItemStack.TAG_LORE);
        } else {
            if (display == null) display = stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY);
            display.put(ItemStack.TAG_LORE, toTag(lore));
        }
        return stack;
    }

    @Override
    public ItemStack label() {
        return EditorUtils.makeLabel(Items.PAPER, "Edit Lore");
    }

    protected void reset() {
        Sounds.clear(player);
        this.stack = getOriginal();
        this.lore = getLore(stack);
        open();
    }

    @Override
    public void open() {
        var elements = new HashMap<Integer, ItemGuiElement>();
        elements.put(10, new ItemGuiElement(EditorUtils.makeLabel(setLore(stack, lore), stack.getHoverName(), "Click to finish"), this::complete));
        elements.put(45, EditorUtils.clear(() -> {
            Sounds.clear(player);
            this.lore.clear();
            open();
        }));
        elements.put(46, EditorUtils.reset(this::reset));
        elements.put(47, EditorUtils.cancel(this::cancel));

        // Divider
        for (int i = 3; i < 54; i += 9)
            elements.put(i, EditorUtils.divider());

        var maxPage = (elements.size() / 5)  - (lore.size() == MAX_LORE ? 1 : 0);
        EditorUtils.drawPage(elements, lore, page, maxPage, newPage -> {
            Sounds.interact(player, 1f + ((float) (newPage + 1) / (maxPage + 1)) / 2);
            this.page = newPage;
            open();
        }, (slot, index) -> {
            var text = lore.get(index);
            elements.put(slot, new ItemGuiElement(EditorUtils.makeLabel(Items.PAPER, text, "Edit Lore"), () -> {
                Sounds.interact(player);
                Menus.component(player, c -> {
                    return new ItemStack(Items.PAPER).setHoverName(c);
                }, text, 50, CancellableCallback.of(c -> {
                    Sounds.success(player);
                    lore.set(index, c);
                    open();
                }, () -> {
                    Sounds.error(player);
                    open();
                }));
            }));
        }, index -> {
            lore.remove((int) index);
            open();
        }, () -> {
            lore.add(literal("Lore"));
            open();
        });

        player.openMenu(EditorUtils.make9x6(Component.literal("Editing Lore"), elements));
    }
}
