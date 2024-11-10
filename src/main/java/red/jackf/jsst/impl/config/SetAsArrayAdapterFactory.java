package red.jackf.jsst.impl.config;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Serializes sets as arrays of elements
 */
public class SetAsArrayAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        if (typeToken.getRawType() != Set.class || !(type instanceof ParameterizedType)) return null;

        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
        //noinspection unchecked
        return (TypeAdapter<T>) newSetAdapter(elementAdapter);
    }

    private <T> TypeAdapter<Set<T>> newSetAdapter(final TypeAdapter<T> elementAdapter) {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, Set<T> set) throws IOException {
                if (set == null) {
                    out.nullValue();
                    return;
                }

                out.beginArray();
                for (T element : set) {
                    elementAdapter.write(out, element);
                }
                out.endArray();
            }

            @Override
            public Set<T> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                Set<T> set = new HashSet<>();

                in.beginArray();
                while (in.hasNext()) {
                    set.add(elementAdapter.read(in));
                }
                in.endArray();
                return set;
            }
        };
    }
}
