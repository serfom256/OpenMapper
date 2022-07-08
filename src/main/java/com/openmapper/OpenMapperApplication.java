package com.openmapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class OpenMapperApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext c = SpringApplication.run(OpenMapperApplication.class, args);
    }


}
