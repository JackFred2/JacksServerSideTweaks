package red.jackf.jsst.feature.itemeditor;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import red.jackf.jsst.JSST;
import red.jackf.jsst.feature.Feature;
import red.jackf.jsst.feature.itemeditor.menus.ItemEditorGui;

public class ItemEditor extends Feature<ItemEditor.Config> {
    public static final ItemEditor INSTANCE = new ItemEditor();
    private ItemEditor() {}

    private static final Logger LOGGER = JSST.getLogger("Item Editor");

    @Override
    public void setup() {}

    @Override
    protected Config config() {
        return JSST.CONFIG.instance().itemEditor;
    }

    public void newSession(ServerPlayer player, ItemStack handItem, @Nullable EquipmentSlot returnSlot) {
        LOGGER.debug("Starting Item Editor for {}, base item {}", player.getName().getString(), handItem);
        new ItemEditorGui(player, handItem, returnSlot).open();
    }

    public static class Config extends Feature.Config {

    }
}
