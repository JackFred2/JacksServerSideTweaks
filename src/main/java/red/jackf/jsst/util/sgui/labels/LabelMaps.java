package red.jackf.jsst.util.sgui.labels;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Collections;

public interface LabelMaps {
    LabelMap<Item> ITEMS = LabelMap.createStatic(Collections.emptyMap(), ItemStack::new);
    LabelMap<MobEffect> MOB_EFFECTS = LabelMap.createDataManaged(BuiltInRegistries.MOB_EFFECT,
                                                                 effect -> PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER),
                                                                 MobEffect::getDisplayName);

    static void touch() {}
}
