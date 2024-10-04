package red.jackf.jsst.feature.beaconenhancement;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.Marshaller;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeaconPowerSet {
    private final SetMultimap<Integer, Holder<MobEffect>> powers = MultimapBuilder.treeKeys().hashSetValues().build();

    public static BeaconPowerSet getDefault() {
        var def = new BeaconPowerSet();
        def.addPower(1, MobEffects.MOVEMENT_SPEED);
        def.addPower(1, MobEffects.DIG_SPEED);
        def.addPower(2, MobEffects.DAMAGE_RESISTANCE);
        def.addPower(2, MobEffects.JUMP);
        def.addPower(3, MobEffects.DAMAGE_BOOST);
        def.addPower(4, MobEffects.REGENERATION);
        def.addPower(5, MobEffects.SLOW_FALLING);
        def.addPower(5, MobEffects.HEALTH_BOOST);
        def.addPower(6, MobEffects.FIRE_RESISTANCE);
        def.addPower(6, MobEffects.NIGHT_VISION);
        return def;
    }

    public void removePower(int level, Holder<MobEffect> effect) {
        this.powers.entries().removeIf(entry -> {
            if (effect != entry.getValue()) return false;
            return level <= 3 ? entry.getKey() <= 3 : entry.getKey() >= 4;
        });
    }

    public void addPower(int level, Holder<MobEffect> effect) {
        if (level < 1 || level > 6) return;

        if (level <= 3)
            for (int i = 1; i <= 3; i++)
                this.getAtLevel(i).remove(effect);
        else
            for (int i = 4; i <= 6; i++)
                this.getAtLevel(i).remove(effect);

        this.powers.put(level, effect);
    }

    public List<Holder<MobEffect>> getPrimaries(int level) {
        List<Holder<MobEffect>> effects = new ArrayList<>();
        for (int i = 1; i <= 3 && i <= level; i++){
            effects.addAll(this.powers.get(i));
        }
        return effects;
    }

    public List<Holder<MobEffect>> getSecondaries(int level) {
        List<Holder<MobEffect>> effects = new ArrayList<>();
        for (int i = 4; i <= 6 && i <= level; i++){
            effects.addAll(this.powers.get(i));
        }
        return effects;
    }

    public Collection<Holder<MobEffect>> getAtLevel(int level) {
        return this.powers.get(level);
    }

    public boolean isValid(MobEffect effect) {
        return this.powers.containsValue(effect);
    }

    public static class Serializer {
        public static BeaconPowerSet deserialize(JsonObject obj, Marshaller marshaller) throws DeserializationException {
            BeaconPowerSet powers = new BeaconPowerSet();
            for (String key : obj.keySet()) {
                try {
                    int level = Integer.parseInt(key);
                    if (level < 1 || level > 6) {
                        throw new DeserializationException("Level must be between 1 and 6, found " + level);
                    }
                    JsonElement elem = obj.get(key);
                    if (elem instanceof JsonArray array) {
                        for (JsonElement effectId : array) {
                            if (effectId instanceof JsonPrimitive prim) {
                                if (prim.getValue() instanceof String) {
                                    Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(marshaller.marshall(ResourceLocation.class, prim));
                                    if (effect != null) {
                                        powers.addPower(level, effect);
                                    } else {
                                        throw new DeserializationException("Unknown effect: " + prim.asString());
                                    }
                                } else {
                                    throw new DeserializationException("Expected string, found " + prim.getClass().getSimpleName());
                                }
                            } else {
                                throw new DeserializationException("Expected string, found " + effectId.getClass().getSimpleName());
                            }
                        }
                    } else {
                        throw new DeserializationException("Expected list, found " + elem.getClass().getSimpleName());
                    }
                } catch (NullPointerException ex) {
                    throw new DeserializationException(ex);
                }
            }
            return powers;
        }

        public static JsonElement serialize(BeaconPowerSet beaconPowerSet, Marshaller marshaller) {
            var root = new JsonObject();
            for (Integer level : beaconPowerSet.powers.keySet()) {
                var set = new JsonArray();
                for (MobEffect effect : beaconPowerSet.powers.get(level)) {
                    ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                    if (id == null) continue;
                    set.add(marshaller.serialize(id));
                }
                root.put(String.valueOf(level), set);
            }
            return root;
        }
    }
}
