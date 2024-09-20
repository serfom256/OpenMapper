package com.openmapper.annotations;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.exceptions.entity.EntityNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.openmapper.common.operations.DmlOperation.SELECT;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DaoMethod {

    String procedure() default "";

    DmlOperation operation() default SELECT;

    Class<? extends Throwable> translateNotFoundExceptionTo() default EntityNotFoundException.class;

    String datasource() default "datasource";

    boolean returnKeys() default false;
}
