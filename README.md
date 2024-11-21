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
Where:
- name.of.query - name of sql procedure
- select count(*) from users... - SQL query
- [QUERY_PARAMETER] - value to replace when procedure will be executed

## Code declaration:

Repository declaration:

```java

@DaoLayer
public interface MyRepo {

    @DaoMethod(procedure = "name.of.query.from.fsql.file") // this name must be equal with procedure name in .fsql file
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

#### Repository:

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

## Object relating mapping:

OpenMapper supports different types of entity objects relationship:

- [x] Many-To-Many
- [x] Many-To-One
- [x] One-To-One

Modeling object relations in code:

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

    // Getters and setters
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

    // Getters and setters
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

    // Getters and setters
}

@Model(primaryKey = "id")
class Course {

    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String data;

    // Getters and setters
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

    // Getters and setters
}

@Model(primaryKey = "id")
class Course {

    @Field
    private Integer id;

    @Field
    private Integer userId;

    @Field
    private String data;

    // Getters and setters
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
}
```

#### Queries in `.fsql` file:

```sql
updateUser = {
    update user set name=[name], version=[version] where version = [oldVersion]  and id = [id]
}

createUser = {
    insert into user(id, name, version) values([id], [name], [version])
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
}
```

#### Queries in `.fsql` file:

```sql
createUser = {
    insert into user(name, version) values([name])
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
### Using @Dto annotation:

#### Dto declaration example:
```java
@Dto
public class DtoUser {

    @Field
    private int id;

    @Field
    private String name;
    
    // Getters and setters
}
```

#### Repository example:
```java
@DaoLayer
public interface Repository {
    @DaoMethod(returnKeys = true, operation = DmlOperation.INSERT)
    int createUser(DtoUser user);
}
```

#### Service call example:
```java
DtoUser user = new DtoUser();
user.setName("user1");
int userId = repository.createUser(user);
```
The main difference between `@Dto` and `@Model` is that @Dto doesn't require primary key specifying, and it cannot be mapped as a query result

---
### Using @Sql annotation:

#### Repository example:
```java
@DaoLayer
public interface Repository {
    @DaoMethod(operation = DmlOperation.SELECT)
    User findUserBy(@Sql query, int id);
}
```

#### Service call example:
```java
int id = 1;
boolean usePagination = true;
boolean useSorting = true;

String query = "SELECT * FROM user WHERE id = [id]";

if (userPagination) {
    query += "OFFSET 10 LIMIT 10";
}
if (useSorting){
    query += "ORDER BY id DESC";
}

User user = repository.findUserBy(query, id);
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
    <version>version</version>
</dependency>
```

#### Usage with Gradle:
```groovy
implementation 'com.openmapper:openmapper-spring-boot-starter:version'
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
| @Cached               | Method level             | Used for query cache for this method, ignores configuration `openmapper.query.cache.enabled`           |
| @Sql                  | Argument level level     | Used for specifying custom query as a string                                                           |
| @Dto                  | Method level             | Used for specifying abstract data type as query parameters                                             |

---

## OpenMapper configuration

| Parameter name                         | Parameter type | Default value                      | Description                                                    |
| -------------------------------------- | -------------- | ---------------------------------- | -------------------------------------------------------------- |
| openmapper.fsql.path                   | list<string>   | all subdirectories in project root | Package to scan for `.fsql` files                              |
| openmapper.dao.packagesToScan          | string         | all subdirectories in project root | Package to scan for `.fsql` files                              |
| openmapper.model.packagesToScan        | string         | all subdirectories in project root | Package to scan for classes, annotated with @Model             |
| openmapper.sql.tracing.enabled         | boolean        | false                              | Tracing sql  queries                                           |
| openmapper.sql.tracing.queries.enabled | boolean        | false                              | Tracing sql that will be executed (don't use it in production) |
| openmapper.logging.enabled             | boolean        | true                               | Enable internal logging                                        |
| openmapper.input.wrapping.enabled      | boolean        | true                               | Wrapping strings in order to protect from SQL Injections       |
| openmapper.query.cache.enabled         | boolean        | false                              | Enable global query cache                                      |

---

## How to install

1) Check `Maven` installation in your system
2) Clone repository
3) Run command `mvn install` inside cloned project

Or

- Run command `git clone git@github.com:serfom256/OpenMapper.git && cd OpenMapper && mvn install`