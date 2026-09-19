package io.github.serfom256.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.serfom256.exceptions.common.SerializationException;

import java.lang.reflect.Type;

public class Jackson2JsonCodec implements Serializer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Object deserialize(String json, Type targetType) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructType(targetType));
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
