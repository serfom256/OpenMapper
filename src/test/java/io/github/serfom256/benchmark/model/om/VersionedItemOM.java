package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.OptimisticLockField;

@Model(primaryKey = "id")
public class VersionedItemOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    @OptimisticLockField(name = "version", oldVersion = "old_version")
    private long version;

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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
