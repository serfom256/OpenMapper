package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;

@Model(primaryKey = "courseId")
public class CourseOM {

    @Field(name = "course_id")
    private long courseId;

    @Field(name = "title")
    private String title;

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
