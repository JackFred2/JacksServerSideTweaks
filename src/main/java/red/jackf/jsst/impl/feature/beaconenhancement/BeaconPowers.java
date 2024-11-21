package red.jackf.jsst.impl.feature.beaconenhancement;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import red.jackf.jsst.impl.utils.RegistryUtils;

import java.util.List;

public record BeaconPowers(List<String> level1,
                           List<String> level2,
                           List<String> level3,
                           List<String> level4,
                           List<String> level5,
                           List<String> level6) {
    public static final BeaconPowers DEFAULT_PRIMARY = new BeaconPowers(
            List.of(key(MobEffects.MOVEMENT_SPEED), key(MobEffects.DIG_SPEED)),
            List.of(key(MobEffects.DAMAGE_RESISTANCE), key(MobEffects.JUMP)),
            List.of(key(MobEffects.DAMAGE_BOOST)),
            List.of(key(MobEffects.GLOWING)),
            List.of(),
            List.of());

    public static final BeaconPowers DEFAULT_SECONDARY = new BeaconPowers(
            List.of(),
            List.of(),
            List.of(),
            List.of(key(MobEffects.REGENERATION)),
            List.of(key(MobEffects.NIGHT_VISION)),
            List.of(key(MobEffects.FIRE_RESISTANCE))
    );

    public BeaconPowers update(int level, List<String> newValues) {
        List<String> copy = List.copyOf(newValues);

        var level1 = level == 1 ? copy : this.level1;
        var level2 = level == 2 ? copy : this.level2;
        var level3 = level == 3 ? copy : this.level3;
        var level4 = level == 4 ? copy : this.level4;
        var level5 = level == 5 ? copy : this.level5;
        var level6 = level == 6 ? copy : this.level6;

        return new BeaconPowers(level1, level2, level3, level4, level5, level6);
    }

    public List<String> get(int level) {
        return switch (level) {
            case 1 -> this.level1;
            case 2 -> this.level2;
            case 3 -> this.level3;
            case 4 -> this.level4;
            case 5 -> this.level5;
            case 6 -> this.level6;
            default -> List.of();
        };
    }

    private static String key(Holder<MobEffect> effect) {
        return effect.unwrapKey().orElseThrow().location().toString();
    }

    public Multimap<Integer, Holder<MobEffect>> parse(RegistryAccess registries) {
        Multimap<Integer, Holder<MobEffect>> map = MultimapBuilder.treeKeys().arrayListValues().build();

        Registry<MobEffect> registry = RegistryUtils.lookup(registries, Registries.MOB_EFFECT);

        // TODO maybe better logging
        for (int level = 1; level <= 6; level++) {
            for (String rawId : this.get(level)) {
                ResourceLocation parsed = ResourceLocation.tryParse(rawId);
                if (parsed == null) continue;
                int finalLevel = level;
                RegistryUtils.getHolder(registry, parsed).ifPresent(ref -> map.put(finalLevel, ref));
            }
        }

        return map;
    }
}
