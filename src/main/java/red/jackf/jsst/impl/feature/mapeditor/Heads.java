package red.jackf.jsst.impl.feature.mapeditor;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface Heads {
    // https://minecraft-heads.com/custom-heads/head/94682-forest-green-arrow-right
    ItemStack RIGHT_ARROW = GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultInstance())
            .setSkullOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM5YzY3YTlmMTY4NWNkMWRhNDNlODQxZmU3ZWJiMTdmNmFmNmVhMTJhN2UxZjI3MjJmNWU3ZjA4OThkYjlmMyJ9fX0=")
            .asStack();

    // https://minecraft-heads.com/custom-heads/head/94681-forest-green-arrow-left
    ItemStack LEFT_ARROW = GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultInstance())
            .setSkullOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWExZWYzOThhMTdmMWFmNzQ3NzAxNDUxN2Y3ZjE0MWQ4ODZkZjQxYTMyYzczOGNjOGE4M2ZiNTAyOTdiZDkyMSJ9fX0=")
            .asStack();
}
