# OpenMapper

OpenMapper is a lightweight ORM library based on JDBC that provides fast object mapping operations

---

## Sql procedures declaration:

OpenMapper supports files [.fsql] extension

`.fsql` file structure:

```sql
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

Model declaration:

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

```sql
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

### Optimistic lock support:


#### Model example:
```java
    @Model(primaryKey = "id")
    public class User {

        @Field
        private int id;

        @Field
        private String name;

        @OptimisticLockField(oldVersion = "oldVersion") // oldVersion - link to previous version to update
        private long version;

        // Getters and setters
```

#### Queries in `.fsql` file:

```sql
    updateUser = {
        update user set name='[name]', version=[version] where version = [oldVersion]  and id = [id]
    }

    createUser = {
        insert into user(id, name, version) values([id], '[name]', [version])
    }

    findUserById = {
        select * from user where id=[id]
    }
```
#### Repository example:
```java
@DaoLayer
public interface Repository {
    @DaoMethod(returnKeys = true, operation = DmlOperation.INSERT)
    int createUser(User user);

    @DaoMethod(operation = DmlOperation.UPDATE) // optimistic lock works only with DmlOperation.UPDATE mode
    int updateUser(User user);

    User findUserById(int id);
}
```

#### Service call example:

```java

    User user = new User(1, "old_name", 1);
    repository.createUser(user); // creating new user with id, name, and default version (if version not specified value will be set to 0)

    repository.updateUser(new User(1, "new_name", user.getVersion())); // updating name and version (user version that equals 1 will be updated to 2)

    System.out.println(repository.findUserById(1)); // will return User(id=1, name=new_name, version=2)
```

### If the previous version is higher than current version, the query will fail due to `version could be incremented only`.
#### Maximum increment version retries count is equal to 100

####  This code produces queries below:
```sql
    insert into user(id, name, version) values(1, 'old_name', 1);

    update user set name='new_name', version=2 where version = 1 and id = 1;

    select * from user where id = 1;
```

---
### Retrieving generated keys after insert:

#### If you specify `returnKeys = true`(by default false) in repository method, then OpenMapper will try to return generated keys and cast them to return type

#### Model example:
```java
    @Model(primaryKey = "id")
    public class User {

        @Field
        private int id;

        @Field
        private String name;

        // Getters and setters
```

#### Queries in `.fsql` file:

```sql
    createUser = {
        insert into user(name, version) values('[name]')
    }
```
#### Repository example:
```java
@DaoLayer
public interface Repository {
    @DaoMethod(returnKeys = true, operation = DmlOperation.INSERT)
    int createUser(User user);
}
```

#### Service call example:
```java
    User user = new User("name", 1);
    int userId = repository.createUser(user);
    System.out.println(userId); // will print 1
```

---

### OpenMapper supports Java types from the table below:

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

#### Usage with Maven:
```xml

<dependency>
    <groupId>com.openmapper</groupId>
    <artifactId>openmapper-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
```

#### Usage with Gradle:
```groovy
implementation 'com.openmapper:openmapper-spring-boot-starter:1.3.2'
```

---

## Supported annotations

| Supported annotations | Type                     | Description                                                                                            |
| --------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------ |
| @Entity               | Class level              | Every declared entity that used in mapping uses the @Model annotation                                  |
| @Field                | Field level              | Used for mapping fields specifying                                                                     |
| @Joined               | Field level              | Used for joining entities by the specified field                                                       |
| @OptimisticLockField  | Field/Argument level     | Used for joining entities by the specified field                                                       |
| @Nested               | Field level              | Used for nested objects                                                                                |
| @DaoLayer             | Class level              | DAO/Repository classes that represents data access layer should be annotated with @DaoLayer annotation |
| @DaoMethod            | Method level             | Used for method that executes the query and returns mapped result                                      |
| @Param                | Argument parameter level | Used for specifying name of the argument that will be substituted in the sql query                     |
| @UseRepository        | Class level              | Used for service layer classes that will be interacting with DAO layer                                 |


---

## How to install

1) Check `Maven` installation in your system
2) Clone repository
3) Run command `mvn install` inside cloned project

Or

- Run command `git clone git@github.com:serfom256/OpenMapper.git && cd OpenMapper && mvn install`