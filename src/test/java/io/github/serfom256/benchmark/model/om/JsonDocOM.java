package io.github.serfom256.benchmark.model.om;

import java.util.List;
import java.util.Map;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Json;
import io.github.serfom256.annotations.entity.Model;

@Model(primaryKey = "id")
public class JsonDocOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    @Field(name = "attributes")
    @Json
    private Map<String, String> attributes;

    @Field(name = "tags")
    @Json
    private List<String> tags;

    @Field(name = "spec")
    @Json
    private JsonSpecOM spec;

    @Field(name = "payload")
    @Json
    private Map<String, Object> payload;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public JsonSpecOM getSpec() {
        return spec;
    }

    public void setSpec(JsonSpecOM spec) {
        this.spec = spec;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
