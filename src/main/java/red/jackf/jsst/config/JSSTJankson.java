package red.jackf.jsst.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.magic.TypeMagic;
import net.minecraft.resources.ResourceLocation;
import red.jackf.jsst.JSST;

import java.lang.reflect.Field;
import java.util.Collection;

public class JSSTJankson {
    public static final Jankson INSTANCE = get();
    public static final JsonGrammar GRAMMAR = JsonGrammar.builder()
            .withComments(true)
            .printTrailingCommas(true)
            .bareSpecialNumerics(true)
            .build();

    static Jankson get() {
        var builder = Jankson.builder();
        builder.registerDeserializer(String.class, ResourceLocation.class, (s, m) -> new ResourceLocation(s));
        builder.registerSerializer(ResourceLocation.class, (rl, m) -> new JsonPrimitive(rl.toString()));
        return builder.build();
    }

    /**
     * Creates a new instance of clazz, but with all fields containing a collection {@link Collection#clear()}ed. This
     * lets collections have default values that dont duplicate each load. Is there a better way? Probably.
     */
    @SuppressWarnings("unused")
    private static <T> T instantiateCleared(Class<T> clazz) {
        try {
            T instance = TypeMagic.createAndCast(clazz);
            for (Field field : clazz.getFields()) {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    var object = field.get(instance);
                    var clearMethod = field.getType().getMethod("clear");
                    clearMethod.invoke(object);
                }
            }
            return instance;
        } catch (Throwable t) {
            JSST.LOGGER.error("Couldn't create a cleared object", t);
            return null;
        }
    }
}