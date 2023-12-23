package red.jackf.jsst.util.sgui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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
        ItemStack defaultLabel = CommonLabels.simple(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), Component.translatable("effect.none"));
        LabelMap<MobEffect> map = new LabelMap<>(defaultLabel);

        for (MobEffect mobEffect : BuiltInRegistries.MOB_EFFECT) {
            ItemStack potion = PotionUtils.setCustomEffects(new ItemStack(Items.POTION), List.of(new MobEffectInstance(mobEffect)));
            ItemStack label = CommonLabels.simple(potion,
                                                  mobEffect.getDisplayName(),
                                                  Component.literal(String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect))).setStyle(Styles.ID));
            map.addMapping(mobEffect, label);
        }

        return map;
    }
}
