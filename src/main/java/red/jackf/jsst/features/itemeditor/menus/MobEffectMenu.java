package red.jackf.jsst.features.itemeditor.menus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import red.jackf.jsst.features.itemeditor.utils.CancellableCallback;
import red.jackf.jsst.features.itemeditor.utils.Labels;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MobEffectMenu {
    private static final Map<MobEffect, ItemStack> ICONS = new LinkedHashMap<>();

    private static void put(MobEffect effect, ItemStack stack) {
        stack.setHoverName(effect.getDisplayName().copy().withStyle(Labels.CLEAN));
        ICONS.put(effect, stack);
    }

    static {
        put(MobEffects.MOVEMENT_SPEED, Labels.create(Items.SUGAR).build());
        put(MobEffects.MOVEMENT_SLOWDOWN, Labels.create(Items.SOUL_SAND).build());
        put(MobEffects.DIG_SPEED, Labels.create(Items.GOLDEN_PICKAXE).build());
        put(MobEffects.DIG_SLOWDOWN, Labels.create(Items.WOODEN_PICKAXE).build());
        put(MobEffects.DAMAGE_BOOST, Labels.create(Items.DIAMOND_SWORD).build());
        put(MobEffects.HEAL, Labels.create(Items.GLISTERING_MELON_SLICE).build());
        put(MobEffects.HARM, Labels.create(Items.FERMENTED_SPIDER_EYE).build());
        put(MobEffects.JUMP, Labels.create(Items.RABBIT_FOOT).build());
        put(MobEffects.CONFUSION, Labels.create(Items.SUSPICIOUS_STEW).build());
        put(MobEffects.REGENERATION, Labels.create(Items.GHAST_TEAR).build());
        put(MobEffects.DAMAGE_RESISTANCE, Labels.create(Items.SHIELD).build());
        put(MobEffects.FIRE_RESISTANCE, Labels.create(Items.MAGMA_CREAM).build());
        put(MobEffects.WATER_BREATHING, Labels.create(Items.PUFFERFISH).build());
        put(MobEffects.INVISIBILITY, Labels.create(Items.GLASS).build());
        put(MobEffects.BLINDNESS, Labels.create(Items.SCULK_SENSOR).build());
        put(MobEffects.NIGHT_VISION, Labels.create(Items.GOLDEN_CARROT).build());
        put(MobEffects.HUNGER, Labels.create(Items.ROTTEN_FLESH).build());
        put(MobEffects.WEAKNESS, Labels.create(Items.WOODEN_SWORD).build());
        put(MobEffects.POISON, Labels.create(Items.SPIDER_EYE).build());
        put(MobEffects.WITHER, Labels.create(Items.WITHER_SKELETON_SKULL).build());
        put(MobEffects.HEALTH_BOOST, Labels.create(Items.APPLE).build());
        put(MobEffects.ABSORPTION, Labels.create(Items.GOLDEN_APPLE).build());
        put(MobEffects.SATURATION, Labels.create(Items.CAKE).build());
        put(MobEffects.GLOWING, Labels.create(Items.SPECTRAL_ARROW).build());
        put(MobEffects.LEVITATION, Labels.create(Items.SHULKER_SHELL).build());
        put(MobEffects.LUCK, Labels.create(Items.ENCHANTED_BOOK).build());
        put(MobEffects.UNLUCK, Labels.create(Items.COARSE_DIRT).build());
        put(MobEffects.SLOW_FALLING, Labels.create(Items.FEATHER).build());
        put(MobEffects.CONDUIT_POWER, Labels.create(Items.CONDUIT).build());
        put(MobEffects.DOLPHINS_GRACE, Labels.create(Items.BIRCH_BOAT).build());
        put(MobEffects.BAD_OMEN, Labels.create(Raid.getLeaderBannerInstance()).build());
        put(MobEffects.HERO_OF_THE_VILLAGE, Labels.create(Items.TOTEM_OF_UNDYING).build());
        put(MobEffects.DARKNESS, Labels.create(Items.ENDER_PEARL).build());
        BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
            if (!ICONS.containsKey(effect))
                put(effect, Labels.create(potionOf(effect)).build());
        });
    }

    private final ServerPlayer player;
    private final CancellableCallback<MobEffect> callback;

    public static ItemStack potionOf(MobEffect effect) {
        var stack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(stack, Potions.WATER);
        PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(effect, 1, 0)));
        return stack;
    }

    protected MobEffectMenu(ServerPlayer player, CancellableCallback<MobEffect> callback) {
        this.player = player;
        this.callback = callback;
    }

    protected void open() {
        Menus.selector(player, ICONS, callback);
    }
}
