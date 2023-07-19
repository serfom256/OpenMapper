package com.openmapper;

import com.openmapper.common.parser.impl.DefaultParserImpl;
import com.openmapper.common.util.FileUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.File;

@EnableAspectJAutoProxy
@SpringBootApplication
public class OpenMapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenMapperApplication.class, args);
        final File file = new File("D:\\src\\Projects\\OpenMapperTest\\src\\main\\resources\\test.fsql");
        var res = new DefaultParserImpl(file.getName(), FileUtil.readFile(file)).parse();
        res.forEach(a -> {
            System.out.println(a);
        });
    }
}
