package red.jackf.jsst.feature.itemeditor.gui.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.ColourMenu;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.GradientMenu;
import red.jackf.jsst.feature.itemeditor.gui.menus.style.ComponentStyleMenu;
import red.jackf.jsst.util.Result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

    static void componentStyle(
            ServerPlayer player,
            Component text,
            Consumer<Optional<Component>> onResult) {
        new ComponentStyleMenu(player, text, onResult).open();
    }

    static void component(ServerPlayer player,
                          Component initial,
                          Function<Component, ItemStack> previewBuilder,
                          Consumer<Component> onResult) {
        new ComponentMenu(player, initial, previewBuilder, onResult).open();
    }
}
