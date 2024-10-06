# OpenMapper

OpenMapper is a lightweight ORM library based on JDBC that provides fast object mapping operations

---

## Sql procedures declaration:

OpenMapper supports files [.fsql] extension

`.fsql` file structure:

```fslq
name.of.query={
    select count(*) from users where id=[QUERY_PARAMETER]
}
```

Where `QUERY` - variable to replacement

---

## OpenMapper properties:

| Property                        | Value                                   | Type    |
| ------------------------------- | --------------------------------------- | ------- |
| openmapper.sql.tracing          | query tracing                           | boolean |
| openmapper.logging              | events tracing                          | boolean |
| openmapper.dao.packagesToScan   | packages to scan for @DaoLayer entities | list    |
| openmapper.model.packagesToScan | packages to scan for @Model entities    | list    |
| openmapper.fsql.path            | packages with `.fsql` files to scan     | list    |

---

## Code declaration:

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

@Model(primaryKey = "id")
public class User {
    @Field(name = "id")
    private int id;
    @Field(name = "name")
    private String name;
    @Joined(joinBy = "id")
    private List<Course> course;

    // Getters and setters
}

@Model(primaryKey = "id", joinedBy = "uId")
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

## Object mapping example:

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

Custom objects that used in sql query must be annotated with `@Model` annotation

#### @Model annotation:

primaryKey: primary key of the table

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

@Model(primaryKey = "id")
class User {
    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String data;

    @Joined(transients = true)
    List<Course> courses;
}

@Model(primaryKey = "id")
class Course {

    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String type;

    @Joined(joinBy = "userId", to = "id")
    List<User> users;
}

interface Repository {
    @DaoMethod
    List<User> getAll();
}

```

### Many To One:

```java

@Model(primaryKey = "id")
class User {

    @Field
    private int id;

    @Field
    private String name;

    @Joined(joinBy = "id", to = "userId")
    private List<Course> courses;
}

@Model(primaryKey = "id")
class Course {

    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String data;
}

interface Repository {
    @DaoMethod
    User getAll();
}

```

### One To One:

```java

@Model(primaryKey = "id")
class User {

    @Field
    private int id;

    @Field
    private String name;

    @Joined(joinBy = "id", to = "userId")
    private Course courses;
}

@Model(primaryKey = "id")
class Course {

    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String data;
}

interface Repository {

    @DaoMethod
    User getAll();
}

```

---

OpenMapper supports Java types from the table below:

| Supported Java mapping types |
| ---------------------------- |
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
    <version>1.2.5</version>
</dependency>
```

Usage with Gradle:

```groovy
implementation 'com.openmapper:openmapper-spring-boot-starter:1.2.5'
```

---

## Supported annotations

| Supported annotations | Type                     | Description                                                                                            |
| --------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------ |
| Entity                | Class level              | Every declared entity that used in mapping uses the @Model annotation                                  |
| Field                 | Field level              | Used for mapping fields specifying                                                                     |
| Joined                | Field level              | Used for joining entities by the specified field                                                       |
| Nested                | Field level              | Used for nested objects                                                                                |
| DaoLayer              | Class level              | DAO/Repository classes that represents data access layer should be annotated with @DaoLayer annotation |
| DaoMethod             | Method level             | Used for method that executes the query and returns mapped result                                      |
| Param                 | Method's parameter level | Used for specifying name of the argument that will be substituted in the sql query                     |
| UseRepository         | Class level              | Used for service layer classes that will be interacting with DAO layer                                 |


---

## How to install

1) Check `Maven` installation in your system
2) Clone repository
3) Run command `mvn install` inside cloned project

Or

- Run command `git clone git@github.com:serfom256/OpenMapper.git && cd OpenMapper && mvn install`