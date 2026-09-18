package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.CourseOM;
import io.github.serfom256.benchmark.model.om.StudentOM;

class ManyToManyTest extends IntegrationTestBase {

    private long insertCourse(String title) {
        try (var connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO course (title) VALUES (?) RETURNING id")) {
            statement.setString(1, title);
            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void link(long studentId, long courseId) {
        try (var connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)")) {
            statement.setLong(1, studentId);
            statement.setLong(2, courseId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long insertStudentRow(String name) {
        try (var connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO student (name) VALUES (?) RETURNING id")) {
            statement.setString(1, name);
            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("reading a many-to-many through a link table collects the far side")
    void readThroughLinkTable() {
        long studentId = insertStudentRow("m2m-" + UUID.randomUUID());
        long first = insertCourse("course-a-" + UUID.randomUUID());
        long second = insertCourse("course-b-" + UUID.randomUUID());
        link(studentId, first);
        link(studentId, second);

        StudentOM student = studentDao.findStudentWithCourses(studentId);

        assertEquals(2, student.getCourses().size());
        assertTrue(student.getCourses().stream().anyMatch(c -> c.getCourseId() == first));
        assertTrue(student.getCourses().stream().anyMatch(c -> c.getCourseId() == second));
    }

    @Test
    @DisplayName("a student with no courses gets an empty collection, not a phantom row")
    void readWithNoLinks() {
        long studentId = insertStudentRow("m2m-empty-" + UUID.randomUUID());

        StudentOM student = studentDao.findStudentWithCourses(studentId);

        assertEquals(0, student.getCourses().size());
    }

    @Test
    @DisplayName("cascade writes the link rows, not the far-side entities")
    void cascadeIntoLinkTable() {
        long first = insertCourse("course-c-" + UUID.randomUUID());
        long second = insertCourse("course-d-" + UUID.randomUUID());

        StudentOM student = new StudentOM();
        student.setName("m2m-write-" + UUID.randomUUID());
        List<CourseOM> courses = new ArrayList<>();
        for (long id : List.of(first, second)) {
            CourseOM course = new CourseOM();
            course.setCourseId(id);
            courses.add(course);
        }
        student.setCourses(courses);

        long studentId = studentDao.insertStudent(student);

        assertEquals(2, studentDao.findStudentWithCourses(studentId).getCourses().size());
    }
}
