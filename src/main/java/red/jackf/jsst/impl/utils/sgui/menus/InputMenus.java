package red.jackf.jsst.impl.utils.sgui.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Consumer;

public interface InputMenus {
    static StringInputMenu.Builder string(ServerPlayer player) {
        return new StringInputMenu.Builder(player);
    }

    static void style(ServerPlayer player, Component text, Consumer<Optional<Component>> onResult) {
        new StyleInputMenu(player, text, onResult).open();
    }
}
