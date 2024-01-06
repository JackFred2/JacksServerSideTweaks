package red.jackf.jsst.feature.itemeditor.gui.editors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
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
            EditorContext context,
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
                              Component.translatable("jsst.itemEditor.name"),
                              this.initial.getHoverName(),
                              newName -> this.initial.copy().setHoverName(newName),
                              result -> {
                                  if (result.hasResult()) {
                                      this.callback.accept(this.initial.copy().setHoverName(result.result()));
                                  } else {
                                      this.callback.accept(this.initial);
                                  }
                              });
    }
}
