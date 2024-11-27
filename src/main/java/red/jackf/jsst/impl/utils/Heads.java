package red.jackf.jsst.impl.utils;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface Heads {
    // https://minecraft-heads.com/custom-heads/head/104673-earth
    ItemStack PMC = create("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU3N2M0ZGUxZjUxYTcwNzIyMDIzZTg1NmI1NDNjZDU3MGYxZDBlZTZiOWQxNjdiNTkwMjhjZTFiYzkyZTQ1OCJ9fX0=");

    static ItemStack create(String headTexture) {
        return GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultInstance())
                .setSkullOwner(headTexture)
                .asStack();
    }
}
