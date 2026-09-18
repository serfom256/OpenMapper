package io.github.serfom256.json;

import java.lang.reflect.Type;

/**
 * Serializes objects to and from JSON for {@code @Field}s mapped to JSON columns.
 *
 * <p>OpenMapper provides Jackson 2 and Jackson 3 implementations chosen at runtime
 * by classpath detection. To use a different library or a customised mapper, define
 * a Spring bean of this type — it takes precedence over the built-in codecs.
 */
public interface Serializer {

    /** Serializes a value to its JSON string representation. */
    String serialize(Object value);

    /**
     * Deserializes JSON text into an instance of the given type. {@code targetType}
     * is the field's generic type, so collections and generics are honoured.
     */
    Object deserialize(String json, Type targetType);
}
