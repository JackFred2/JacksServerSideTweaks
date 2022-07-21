package red.jackf.jsst.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.magic.TypeMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import red.jackf.jsst.JSST;

import java.lang.reflect.Field;
import java.util.Collection;

public class JSSTJankson {
    public static final Jankson INSTANCE = get();

    static Jankson get() {
        var builder = Jankson.builder();
        builder.registerDeserializer(String.class, ResourceLocation.class, (s, m) -> new ResourceLocation(s));
        builder.registerSerializer(ResourceLocation.class, (rl, m) -> new JsonPrimitive(rl.toString()));
        builder.registerTypeFactory(JSSTConfig.PortableCrafting.class, () -> instantiateCleared(JSSTConfig.PortableCrafting.class));
        return builder.build();
    }

    /**
     * Creates a new instance of clazz, but with all fields containing a collection {@link Collection#clear()}ed. This
     * lets collections have default values that dont duplicate each load. Is there a better way? Probably.
     */
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

    static String tabToString(String in) {
        return StringUtils.replace(in, "\t", "  ");
    }
}
