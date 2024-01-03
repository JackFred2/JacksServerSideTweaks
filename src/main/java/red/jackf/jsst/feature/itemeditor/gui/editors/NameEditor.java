package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.menus.EditorMenus;
import red.jackf.jsst.util.sgui.Sounds;

import java.util.function.Consumer;

public class NameEditor implements Editor {
    public static EditorType TYPE = new EditorType(
            NameEditor::new,
            true,
            ignored -> true,
            () -> GuiElementBuilder.from(Items.ANVIL.getDefaultInstance())
                                   .setName(Component.translatable("jsst.itemEditor.name"))
    );
    private final ServerPlayer player;
    private final ItemStack initial;
    private final Consumer<ItemStack> callback;

    public NameEditor(
            ServerPlayer player,
            boolean ignored,
            ItemStack initial,
            Consumer<ItemStack> callback) {
        this.player = player;
        this.initial = initial;
        this.callback = callback;
    }

    @Override
    public void run() {
        Sounds.click(player);
        EditorMenus.component(player,
                              this.initial.getHoverName(),
                              newName -> this.initial.copy().setHoverName(newName),
                              newName -> callback.accept(this.initial.copy().setHoverName(newName)));
    }
}
