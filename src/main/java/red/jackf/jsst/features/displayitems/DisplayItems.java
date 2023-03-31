package red.jackf.jsst.features.displayitems;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Feature;

public class DisplayItems extends Feature<DisplayItems.Config> {
    private static final int INFINITE_LIFETIME = -32768;

    public static boolean isDisplayItem(ItemStack stack) {
        return stack.hasCustomHoverName() && stack.getHoverName().getString().equals("[display]");
    }

    @Override
    public void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!this.getConfig().enabled) return;
            if (entity instanceof ItemEntity item && isDisplayItem(item.getItem())) {
                if (item.getAge() == INFINITE_LIFETIME) return; // already done
                item.setUnlimitedLifetime();
                if (item.getOwner() instanceof Player) level.playSound(null, item, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1f, 1f);
                if (getConfig().ownerPickupOnly && item.getOwner() instanceof Player) {
                    item.setTarget(item.getOwner().getUUID());
                }
            }
        });
    }

    @Override
    public String id() {
        return "displayItems";
    }

    @Override
    public Config getConfig() {
        return JSST.CONFIG.get().displayItems;
    }

    @Override
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(OptionBuilders.withBoolean("ownerPickupOnly", () -> getConfig().ownerPickupOnly, value -> getConfig().ownerPickupOnly = value));
    }

    public static class Config extends Feature.Config {
        @Comment("Should only the owner be allowed to pick up this item? (Default: true, Options: true, false)")
        public boolean ownerPickupOnly = true;
    }
}
