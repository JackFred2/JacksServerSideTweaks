package red.jackf.jsst.impl.utils.sgui.menus;

import net.minecraft.server.level.ServerPlayer;

public interface InputMenus {
    static StringInputMenu.Builder string(ServerPlayer player) {
        return new StringInputMenu.Builder(player);
    }

}
