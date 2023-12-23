package red.jackf.jsst.util.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

public interface LabelMaps {
    LabelMap<MobEffect> MOB_EFFECTS = createMobEffects();

    // TODO replace with data
    static LabelMap<MobEffect> createMobEffects() {
        var map = new LabelMap<MobEffect>(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        for (MobEffect mobEffect : BuiltInRegistries.MOB_EFFECT) {
            map.addMapping(mobEffect, PotionUtils.setCustomEffects(new ItemStack(Items.POTION), List.of(new MobEffectInstance(mobEffect))));
        }

        return map;
    }
}
