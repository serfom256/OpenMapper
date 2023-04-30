# OpenMapper

OpenMapper is a lightweight ORM library that provides blazingly fast object mapping operations and query execution
because it uses JDBC

---

## Sql procedures declaration:

OpenMapper supports files [.fsql] extension

`.fsql` file structure:

```fslq
name.of.query={
    select count(*) from users where id=[id]
}
```

---

## Setup OpenMapper properties:

There are `fsql.files` - files, which contains declared sql methods
and `packagesToScan` - package to scan for declared repositories with `@DaoLayer` annotation


---

Repository declaration:

```java

@DaoLayer
public interface MyRepo {

    @DaoMethod(procedure = "name.of.query.from.fsql.file")
    int getUsersCount();
}
```

---

Entity declaration:

```java

@Entity(primaryKey = "id")
public class User {
    @Field(name = "id")
    private int id;
    @Field(name = "name")
    private String name;
    @Joined(joinBy = "id")
    private List<Course> course;

    // Getters and setters
}

@Entity(primaryKey = "id", joinedBy = "uId")
public class Course {
    @Field(name = "crs.id")
    private int id;
    @Field(name = "user_id")
    private int uId;
    @Field(name = "data")
    private String data;

    // Getters and setters
}
```

## Objects mapping:

#### Sql procedure:

```
select.all.users={
    select * from user join course crs on user.id = crs.user_id where user.id>[id]
}
```

#### Repository template:

```java

@DaoLayer
public interface MyRepo {

    @DaoMethod(procedure = "select.all.users")
    List<User> getAll(@Param(name = "id") Integer id);
}
```

Custom objects that used in sql query must be annotated with `@Entity` annotation

#### @Entity annotation:

primaryKey: primary key of the table

joinedBy: field that used in joining

#### @Field annotation:

name: name in sql procedure declared in: `.fsql` file

#### @Param annotation:

name: name in sql procedure that will be replaced with

---

## Object mapping relationship:

OpenMapper supports different types of entity objects relationship:

- [x] Many-To-Many
- [x] Many-To-One
- [x] One-To-One

Modeling objects relationship in code:

### Many To Many:

```java
class User {
    // User's fields
    List<Course> courses;
}

class Course {
    // Course's fields
}

interface Repository {
    @DaoMethod(procedure = "")
    List<User> getAll();
}

```

### Many To One:

```java
class User {
    // User's fields
    List<Course> courses;
}

class Course {
    // Course's fields
}

interface Repository {
    @DaoMethod(procedure = "")
    User getAll();
}

```

### One To One:

```java
class User {
    // User's fields
    Course courses;
}

class Course {
    // Course's fields
}

interface Repository {
    @DaoMethod(procedure = "")
    User getAll();
}

```

---

OpenMapper supports Java types from table below:

| Supported Java mapping types |
|------------------------------|
| Integer / int                |
| Long / long                  |
| Double / double              |
| Float / float                |
| Short / short                |
| Byte / byte                  |
| Boolean / boolean            |
| String                       |
| Object                       |
| java.sql.Date                |
| java.sql.Time                |
| TimeStamp                    |
| BigDecimal                   |
| Blob                         |
| Iterable                     |
| List                         |
| AbstractList                 |
| LinkedList                   |
| ArrayList                    |
| Collection                   |

---

Usage with Maven:

```xml

<dependency>
    <groupId>com.openmapper</groupId>
    <artifactId>openmapper-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Usage with Gradle:

```groovy
implementation 'com.openmapper:openmapper-spring-boot-starter:1.0.0'
```