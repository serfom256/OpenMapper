package io.github.serfom256.json;

import io.github.serfom256.exceptions.common.SerializationException;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

public class Jackson3JsonCodec implements Serializer {

    private final ObjectMapper mapper = JsonMapper.builder().build();

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
