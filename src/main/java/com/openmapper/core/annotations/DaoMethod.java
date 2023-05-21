package com.openmapper.core.annotations;

import com.openmapper.common.DmlOperation;
import com.openmapper.exceptions.entity.EntityNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.openmapper.common.DmlOperation.SELECT;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DaoMethod {
    String procedure() default "";

    DmlOperation operation() default SELECT;

    boolean exceptionIfNotFound() default true;

    Class<? extends Throwable> exceptionIfNot() default EntityNotFoundException.class;

    String datasource() default "datasource";
}
