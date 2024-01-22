package red.jackf.jsst.feature.itemeditor.gui.editors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.itemeditor.gui.EditorContext;
import red.jackf.jsst.util.sgui.Sounds;
import red.jackf.jsst.util.sgui.elements.JSSTElementBuilder;
import red.jackf.jsst.util.sgui.menus.Menus;

import java.util.function.Consumer;

public class PlayerHeadEditor implements Editor {
    public static EditorType TYPE = new EditorType(
            JSST.id("player_head"),
            PlayerHeadEditor::new,
            true,
            false,
            stack -> stack.is(Items.PLAYER_HEAD),
            context -> JSSTElementBuilder.ui(Items.PLAYER_HEAD)
                    .setName(Component.translatable("jsst.itemEditor.playerHead"))
                    .setSkullOwner(context.player().getGameProfile(), null)
    );
    private final ServerPlayer player;
    private final ItemStack stack;
    private final Consumer<ItemStack> callback;

    public PlayerHeadEditor(ServerPlayer player, EditorContext context, ItemStack stack, Consumer<ItemStack> callback) {
        this.player = player;
        this.stack = stack;
        this.callback = callback;
    }

    private String getName() {
        CompoundTag tag = this.stack.getTag();
        if (tag != null) {
            if (tag.contains(PlayerHeadItem.TAG_SKULL_OWNER, Tag.TAG_STRING)) {
                return tag.getString(PlayerHeadItem.TAG_SKULL_OWNER);
            } else if (tag.contains(PlayerHeadItem.TAG_SKULL_OWNER, Tag.TAG_COMPOUND)) {
                CompoundTag ownerTag = tag.getCompound(PlayerHeadItem.TAG_SKULL_OWNER);
                if (ownerTag.contains(ItemStack.TAG_DISPLAY_NAME, Tag.TAG_STRING)) {
                    return ownerTag.getString(ItemStack.TAG_DISPLAY_NAME);
                }
            }
        }

        return this.player.getGameProfile().getName();
    }

    private void setName(String name) {
        this.stack.getOrCreateTag().putString(PlayerHeadItem.TAG_SKULL_OWNER, name);
    }

    @Override
    public void run() {
        Sounds.click(this.player);
        String name = getName();

        Menus.stringBuilder(this.player)
                .title(Component.translatable("jsst.itemEditor.playerHead"))
                .initial(name)
                .createAndShow(result -> {
                    if (result.hasResult())
                        setName(result.result());
                    this.callback.accept(this.stack);
                });
    }
}
