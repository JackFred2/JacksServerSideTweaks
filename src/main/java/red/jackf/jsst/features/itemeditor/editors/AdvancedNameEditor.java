package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.itemeditor.menus.Menus;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

public class AdvancedNameEditor extends Editor {
    public AdvancedNameEditor(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
        super(stack, player, completeCallback);
    }

    @Override
    public ItemStack label() {
        return Labels.create(Items.ANVIL).withName("Edit Name (Advanced)").build();
    }

    @Override
    public void open() {
        Menus.component(player, this::previewBuilder, stack.hasCustomHoverName() ? stack.getHoverName() : null, 50, CancellableCallback.of(c -> {
            if (c.getString().isEmpty()) stack.resetHoverName();
            else stack.setHoverName(c);
            complete();
        }, this::cancel));
    }

    private ItemStack previewBuilder(Component c) {
        var preview = stack.copy();
        if (c.getString().isEmpty()) {
            preview.resetHoverName();
        } else {
            preview.setHoverName(c);
        }
        return preview;
    }
}
