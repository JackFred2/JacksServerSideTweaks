package red.jackf.jsst.feature.beaconpowers;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.Marshaller;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.*;

public class BeaconPowers {
    private final Multimap<Integer, MobEffect> powers = MultimapBuilder.treeKeys().hashSetValues().build();

    public static BeaconPowers getDefault() {
        var def = new BeaconPowers();
        def.addPower(1, MobEffects.MOVEMENT_SPEED);
        def.addPower(1, MobEffects.DIG_SPEED);
        def.addPower(2, MobEffects.DAMAGE_RESISTANCE);
        def.addPower(2, MobEffects.JUMP);
        def.addPower(3, MobEffects.DAMAGE_BOOST);
        def.addPower(4, MobEffects.REGENERATION);
        return def;
    }

    public void addPower(int level, MobEffect effect) {
        if (level < 1 || level > 6) return;

        for (Iterator<Map.Entry<Integer, MobEffect>> iterator = this.powers.entries().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MobEffect> entry = iterator.next();
            if (entry.getValue() == effect) {
                if (entry.getKey() > level) iterator.remove();
                else return;
            }
        }
        this.powers.put(level, effect);
    }

    public List<MobEffect> getPrimaries(int level) {
        List<MobEffect> effects = new ArrayList<>();
        for (int i = 1; i <= 3 && i <= level; i++){
            effects.addAll(this.powers.get(i));
        }
        return effects;
    }

    public List<MobEffect> getSecondaries(int level) {
        List<MobEffect> effects = new ArrayList<>();
        for (int i = 4; i <= 6 && i <= level; i++){
            effects.addAll(this.powers.get(i));
        }
        return effects;
    }

    public boolean isValid(MobEffect effect) {
        return this.powers.containsValue(effect);
    }

    public static class Serializer {
        public static BeaconPowers deserialize(JsonObject obj, Marshaller marshaller) throws DeserializationException {
            BeaconPowers powers = new BeaconPowers();
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
                                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(marshaller.marshall(ResourceLocation.class, prim));
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

        public static JsonElement serialize(BeaconPowers beaconPowers, Marshaller marshaller) {
            var root = new JsonObject();
            for (Integer level : beaconPowers.powers.keySet()) {
                var set = new JsonArray();
                for (MobEffect effect : beaconPowers.powers.get(level)) {
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
