package red.jackf.jsst.features.itemeditor.editors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.function.Consumer;

public class UnbreakableEditors {
    public static class MakeUnbreakable extends Editor {
        public MakeUnbreakable(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
            super(stack, player, completeCallback);
        }

        @Override
        public boolean applies(ItemStack stack) {
            var tag = stack.getTag();
            return stack.getItem().getMaxDamage() > 0 && (tag == null || !tag.getBoolean("Unbreakable"));
        }

        @Override
        public ItemStack label() {
            return Labels.create(Items.STONE_BRICKS).withName("Make Unbreakable").build();
        }

        @Override
        public void playOpenSound() {}

        @Override
        public void open() {
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
            Sounds.success(player);
            complete();
        }
    }

    public static class RemoveUnbreakable extends Editor {
        public RemoveUnbreakable(ItemStack stack, ServerPlayer player, Consumer<ItemStack> completeCallback) {
            super(stack, player, completeCallback);
        }

        @Override
        public boolean applies(ItemStack stack) {
            var tag = stack.getTag();
            return stack.getItem().getMaxDamage() > 0 && tag != null && tag.getBoolean("Unbreakable");
        }

        @Override
        public ItemStack label() {
            return Labels.create(Items.CRACKED_STONE_BRICKS).withName("Remove Unbreakable").build();
        }

        @Override
        public void playOpenSound() {}

        @Override
        public void open() {
            stack.getOrCreateTag().remove("Unbreakable");
            Sounds.success(player);
            complete();
        }
    }
}
