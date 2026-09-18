package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;

public abstract class AuditedBase {

    @Field(name = "id")
    private long id;

    @Field(name = "created_by")
    private String createdBy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
