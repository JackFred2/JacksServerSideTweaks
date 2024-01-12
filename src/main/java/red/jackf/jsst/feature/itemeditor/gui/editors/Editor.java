package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an action type to be done to an itemstack.
 */
public interface Editor {

    void run();

    interface Constructor<E extends Editor> {
        E create(ServerPlayer viewer, EditorContext cosmeticOnly, ItemStack initial, Consumer<ItemStack> callback);
    }

    record EditorType(ResourceLocation id,
                      Constructor<?> constructor,
                      boolean cosmeticOnly,
                      boolean developer,
                      Predicate<ItemStack> appliesTo,
                      Function<EditorContext, GuiElementBuilderInterface<?>> labelSupplier) {}
}
