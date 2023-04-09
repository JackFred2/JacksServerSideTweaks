package red.jackf.jsst.features.itemeditor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.minecraft.network.chat.Component.literal;

public class EditorUtils {
    static final ItemStack BLANK = new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setHoverName(literal(""));
    static final ItemStack DIVIDER = new ItemStack(Items.LIME_STAINED_GLASS_PANE).setHoverName(literal(""));

    static MenuProvider makeMenu(Component title, Map<Integer, ItemButton> buttons) {
        return new SimpleMenuProvider((i, inventory, player) -> {
            var menu = ChestMenu.threeRows(i, inventory);
            for (var slot : menu.slots) {
                if (slot.container != menu.getContainer()) continue;
                if (buttons.containsKey(slot.index)) {
                    slot.set(buttons.get(slot.index).label);
                } else {
                    slot.set(BLANK.copy());
                }
            }
            ((JSSTSealableMenuWithButtons) menu).jsst_sealWithButtons(buttons);
            return menu;
        }, title);
    }

    public record ItemButton(ItemStack label, @Nullable Runnable onClick) {}
}
