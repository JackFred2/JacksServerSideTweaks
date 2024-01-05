package red.jackf.jsst.util.sgui.labels;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Collections;
import java.util.Map;

public interface LabelMaps {
    LabelMap<Item> ITEMS = LabelMap.createStatic(Collections.emptyMap(), ItemStack::new);
    LabelMap<MobEffect> MOB_EFFECTS = LabelMap.createDataManaged(BuiltInRegistries.MOB_EFFECT,
                                                                 effect -> Items.POTION.getDefaultInstance(),
                                                                 MobEffect::getDisplayName);
    LabelMap<Potion> POTIONS = LabelMap.createStatic(Map.of(
            Potions.EMPTY,
            Items.GLASS_BOTTLE.getDefaultInstance().setHoverName(Component.translatable("jsst.itemEditor.potionEditor.noPotion"))
    ), potion -> {
        var stack = Items.POTION.getDefaultInstance();
        PotionUtils.setPotion(stack, potion);
        stack.setHoverName(Component.translatable(potion.getName(Items.POTION.getDescriptionId() + ".effect.")));
        return stack;
    });

    static void touch() {}
}
