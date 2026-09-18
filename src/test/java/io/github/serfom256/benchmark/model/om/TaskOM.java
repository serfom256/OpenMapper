package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.EnumType;
import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;

@Model(primaryKey = "id")
public class TaskOM {

    public enum Status { OPEN, IN_PROGRESS, DONE }

    public enum Priority { LOW, HIGH }

    @Field(name = "id")
    private long id;

    @Field(name = "title")
    private String title;

    @Field(name = "status", enumType = EnumType.ORDINAL)
    private Status status;

    @Field(name = "priority")
    private Priority priority;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
