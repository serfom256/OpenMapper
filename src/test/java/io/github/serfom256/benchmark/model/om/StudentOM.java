package io.github.serfom256.benchmark.model.om;

import java.util.List;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Model;

@Model(primaryKey = "id")
public class StudentOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    @Joined(joinBy = "id", to = "student_id", saveProcedure = "linkStudentCourse")
    private List<CourseOM> courses;

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

    public List<CourseOM> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseOM> courses) {
        this.courses = courses;
    }
}
