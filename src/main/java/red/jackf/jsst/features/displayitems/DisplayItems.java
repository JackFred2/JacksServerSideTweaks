package red.jackf.jsst.features.displayitems;

import blue.endless.jankson.Comment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import red.jackf.jsst.JSST;
import red.jackf.jsst.command.OptionBuilders;
import red.jackf.jsst.features.Sounds;
import red.jackf.jsst.features.ToggleableFeature;

public class DisplayItems extends ToggleableFeature<DisplayItems.Config> {
    private static final int INFINITE_LIFETIME = -32768;
    private static final String JSST_DISPLAY_TAG = "jsst_display_item";

    public static boolean isDisplayItem(ItemStack stack) {
        return stack.hasCustomHoverName() && stack.getHoverName().getString().equals("[display]");
    }

    @Override
    public void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!this.getConfig().enabled) return;
            if (entity instanceof ItemEntity item && isDisplayItem(item.getItem()) && item.getOwner() instanceof ServerPlayer player) {
                if (!level.getServer().isSingleplayer() && getConfig().operatorOnly && !level.getServer().getPlayerList().isOp(player.getGameProfile())) return; // not allowed
                if (item.getAge() == INFINITE_LIFETIME) return; // already done
                item.setUnlimitedLifetime();
                item.addTag(JSST_DISPLAY_TAG);
                Sounds.success(player);
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
    public void setupCommand(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext buildContext) {
        node.then(
                OptionBuilders.withBoolean("ownerPickupOnly", () -> getConfig().ownerPickupOnly, value -> getConfig().ownerPickupOnly = value)
        ).then(
                OptionBuilders.withBoolean("operatorOnly", () -> getConfig().operatorOnly, value -> getConfig().operatorOnly = value)
        );
    }

    public static class Config extends ToggleableFeature.Config {
        @Comment("Should only the owner be allowed to pick up display items? (Default: true, Options: true, false)")
        public boolean ownerPickupOnly = true;
        @Comment("Should only server operators be allowed to create display items? Does not apply in single player. Recommended to enable ownerPickupOnly if this is true. (Default: true, Options: true, false)")
        public boolean operatorOnly = false;
    }
}
