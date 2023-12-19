package red.jackf.jsst.feature.portablecrafting;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.ToggleFeature;

public class PortableCrafting extends ToggleFeature<PortableCrafting.Config> {
    public static PortableCrafting INSTANCE = new PortableCrafting();
    private TagKey<Item> tag;

    private PortableCrafting() {}

    public void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!(player instanceof ServerPlayer)) return InteractionResultHolder.pass(ItemStack.EMPTY);
            if (!config().enabled) return InteractionResultHolder.pass(ItemStack.EMPTY);
            if (config().requireSneak && !player.isShiftKeyDown()) return InteractionResultHolder.pass(ItemStack.EMPTY);

            ItemStack handItem = player.getItemInHand(hand);
            if (handItem.is(tag)) {
                player.openMenu(new SimpleMenuProvider(PortableCrafting::createMenu, handItem.getHoverName()));
                return InteractionResultHolder.success(ItemStack.EMPTY);
            } else {
                return InteractionResultHolder.pass(ItemStack.EMPTY);
            }
        });
    }

    private static CraftingMenu createMenu(int i, Inventory inventory, Player player) {
        return new CraftingMenu(i, inventory, new ExecutingFalsePosAccess(player.level()));
    }

    protected PortableCrafting.Config config() {
        return JSST.CONFIG.instance().portableCrafting;
    }

    @Override
    public void reload(Config current, @Nullable Config old) {
        super.reload(current, old);
        this.tag = TagKey.create(Registries.ITEM, current.itemTag);
    }

    public static class Config extends ToggleFeature.Config {
        @Comment("""
                Whether crafting table items require the player to sneak in order to open.
                Options: true, false
                Default: false""")
        public boolean requireSneak = false;

        @Comment("""
                A tag containing items that should count as crafting tables on right-click.
                Options: true, false
                Default: false""")
        public ResourceLocation itemTag = JSST.id("crafting_tables");
    }
}
