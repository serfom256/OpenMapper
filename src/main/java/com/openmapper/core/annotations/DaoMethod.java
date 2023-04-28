package com.openmapper.core.annotations;

import com.openmapper.exceptions.EntityNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DaoMethod {
    String procedure();

    boolean exceptionIfNotFound() default true;

    Class<? extends Throwable> exceptionIfNot() default EntityNotFoundException.class;

    String datasource() default "datasource";
}
