package red.jackf.jsst.feature.itemeditor.menus.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface Editor {

    default boolean appliesTo(ItemStack stack) {
        return true;
    }

    GuiElementBuilder getLabel();

    void start();

    interface Supplier<E extends Editor> {
        E create(ServerPlayer viewer, ItemStack initial, Consumer<ItemStack> callback);
    }
}
