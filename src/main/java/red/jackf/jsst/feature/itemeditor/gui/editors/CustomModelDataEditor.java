package red.jackf.jsst.feature.itemeditor.gui.editors;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.Sounds;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.function.Consumer;

public class CustomModelDataEditor implements Editor {
    private static final String TAG = "CustomModelData";

    public static EditorType TYPE = new EditorType(
            JSST.id("custom_model_data"),
            CustomModelDataEditor::new,
            true,
            false,
            ignored -> true,
            context -> JSSTElementBuilder.from(Items.SPAWNER)
                    .hideFlags()
                    .setName(Component.translatable("jsst.itemEditor.customModelData"))
    );
    private final ServerPlayer player;
    private final ItemStack initial;
    private final ItemStack stack;
    private final Consumer<ItemStack> callback;

    public CustomModelDataEditor(ServerPlayer player, EditorContext context, ItemStack stack, Consumer<ItemStack> callback) {
        this.player = player;
        this.initial = stack.copy();
        this.stack = stack;
        this.callback = callback;
    }

    @Override
    public void run() {
        Sounds.click(player);
        var tag = stack.getTag();
        int initial = (tag != null && tag.contains(TAG, Tag.TAG_INT)) ? tag.getInt(TAG) : 0;

        Menus.integer(player,
                Component.translatable("jsst.itemEditor.customModelData"),
                initial,
                null,
                null,
                null,
                result -> {
                    if (result.hasResult()) {
                        this.stack.getOrCreateTag().putInt(TAG, result.result());
                        this.callback.accept(this.stack);
                    } else {
                        this.callback.accept(this.initial);
                    }
                });
    }
}
