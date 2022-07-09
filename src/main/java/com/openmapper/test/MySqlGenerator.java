package com.openmapper.test;

import com.openmapper.core.annotations.SqlGenerator;
import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.SqlName;

import java.util.Date;


@SqlGenerator
public interface MySqlGenerator {

    @DaoMethod(sqlName = "name.of.query.6")
    String generateSql(@SqlName(name = "id") Integer a, @SqlName(name = "name") String b);


    @DaoMethod(sqlName = "name.of.query.0")
    String mapSql(@SqlName(name = "table1") String t1, @SqlName(name = "table2") String t2,
                  @SqlName(name = "utcTime") Date time1, @SqlName(name = "utcTime1") Date time2);

    @DaoMethod(sqlName = "name.of.query.8")
    String updateSalarySql(@SqlName(name = "t1") String table, @SqlName(name = "salary") Integer salary, @SqlName(name = "id") Integer id);

}
