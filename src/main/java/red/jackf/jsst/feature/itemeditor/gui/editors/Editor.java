package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an action type to be done to an itemstack.
 */
public interface Editor {

    void run();

    interface Constructor<E extends Editor> {
        E create(ServerPlayer viewer, boolean cosmeticOnly, ItemStack initial, Consumer<ItemStack> callback);
    }

    record EditorType(Constructor<?> constructor,
                      boolean cosmeticOnly,
                      Predicate<ItemStack> appliesTo,
                      Supplier<GuiElementBuilderInterface<?>> labelSupplier) {}
}
