package red.jackf.jsst.impl.utils.sgui.menus;

import net.minecraft.server.level.ServerPlayer;
import red.jackf.jsst.impl.utils.sgui.menus.selection.SelectionMenu;

public interface InputMenus {
    static StringInputMenu.Builder string(ServerPlayer player) {
        return new StringInputMenu.Builder(player);
    }

    static <T> SelectionMenu.Builder<T> selection(ServerPlayer player) {
        return new SelectionMenu.Builder<>(player);
    }
}
