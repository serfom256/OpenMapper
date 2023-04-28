# OpenMapper

### Provides to load sql from files with extension [.fsql]
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
