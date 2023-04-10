package red.jackf.jsst.features.itemeditor;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ItemGuiElement(ItemStack label, @Nullable Runnable onClick) {
}
