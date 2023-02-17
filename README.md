# OpenMapper

### Provides to load sql from files with extension [.fsql] (File SQL)
### Mapping features will be implemented soon 


Firstly, you need to create file with [.fsql] extension with sql methods declaration

Then you need to specify OpenMapper properties in application.properties
There are `fsql.files` - files, which contains declared sql methods
and `packagesToScan` - package to scan for `@SqlGenerator` annotation

```propoerties
openmapper.fsql.files=src/main/resources/test.fsql, src/main/resources/file1.fsql
openmapper.packagesToScan=com.openmapper.test
```

file.fsql
```fslq
name.of.query.1={
    UPDATE table1 SET salary=1000 WHERE id=5
}

name.of.query.6={
    UPDATE table1(name) SET name=[name] WHERE id=[id]
}


name.of.query.8={
    SELECT * FROM TABLE[t1] WHERE salary=[salary] AND id=[id]
}
```

After that you should create an interface with the annotation `@SqlGenerator` and methods with the annotation `@DaoMethod(sqlName = "")`
The annotation `@DaoMethod` annotation allows you extracting sql by the sql name which should be equals to the name of the sql method in .fsql file
##

```java
import org.springframework.beans.factory.annotation.Autowired;

@SqlGenerator
public interface MySqlGenerator {

    @DaoMethod(sqlName = "name.of.query.6")
    String update1(@SqlName(name = "id") Integer a, @SqlName(name = "name") String b);

    @DaoMethod(sqlName = "name.of.query.8")
    String updateSalarySql(@SqlName(name = "t1") String table, @SqlName(name = "salary") Integer salary, @SqlName(name = "id") Integer id);

}

@Repo
class MyRepository {

    final MySqlGenerator generator;

    @Autowired
    public Runner(MySqlGenerator generator) {
        this.generator = generator;
    }

    public void test() {
        String sql1 = generator.generateSql(1000, "value");
        String sql2 = generator.updateSalarySql("tab1", 1000, 11);
        asserEuals("UPDATE table1(name) SET name=value WHERE id=1000", sql1);
        asserEuals("SELECT * FROM TABLE tab1 WHERE salary=1000 AND id=11 ", sql2);
    }
}
```

