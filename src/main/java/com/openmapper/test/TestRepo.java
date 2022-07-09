package com.openmapper.test;

import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Date;

@Repository
public class TestRepo {

    private final MySqlGenerator generator;


    public TestRepo(MySqlGenerator generator) {
        this.generator = generator;
    }

    @PostConstruct
    public void test() {
        Integer a = 1000;
        String b = "value";
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
//            String.format("UPDATE table1(name) SET name=[%s] WHERE id=[%s]", a, b);
//            String.format("SELECT * FROM [%s] t1\n" +
//                    "    JOIN [%s] t2\n" +
//                    "    USING(id)\n" +
//                    "    WHERE t1.[%s] > t2.[%s]", "tab1", "tab2", new Date(), new Date());
//
//            String.format("UPDATE [%s](salary) SET salary=[%s] WHERE id=[%s]", "tab1", 1000, 11);
            generator.generateSql(a, b);
            generator.mapSql("tab1", "tab2", new Date(), new Date());
            generator.updateSalarySql("tab1", 1000, 11);
        }

        System.out.println(System.currentTimeMillis() - start);
        System.out.println(generator.generateSql(a, b));
        System.out.println(generator.mapSql("tab1", "tab2", new Date(), new Date()));
        System.out.println(generator.updateSalarySql("tab1", 1000, 11));
    }
}
