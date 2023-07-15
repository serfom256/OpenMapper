package com.openmapper;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.parser.FileParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.File;
import java.util.List;

@EnableAspectJAutoProxy
@SpringBootApplication
public class OpenMapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenMapperApplication.class, args);
        List<SQLProcedure> parse =
                new FileParser().parse(new File("D:\\src\\Projects\\OpenMapperTest\\src\\main\\resources\\test.fsql"));
        System.out.println(parse);
    }
}
