package red.jackf.jsst.feature.itemeditor.gui.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.ColourMenu;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.GradientMenu;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.StyleMenu;
import red.jackf.jsst.util.Result;

import java.util.Optional;
import java.util.function.Consumer;

public interface EditorMenus {

    static void colour(
            ServerPlayer player,
            Consumer<Result<Colour>> callback) {
        new ColourMenu(player, false, callback).open();
    }

    static void removableColour(
            ServerPlayer player,
            Consumer<Result<@Nullable Colour>> callback) {
        new ColourMenu(player, true, callback).open();
    }

    static void gradient(
            ServerPlayer player,
            Consumer<Result<Gradient>> callback) {
        new GradientMenu(player, callback).open();
    }

    static void style(
            ServerPlayer player,
            Component text,
            Consumer<Optional<Component>> onResult) {
        new StyleMenu(player, text, onResult).open();
    }
}
