package com.openmapper.annotations;

import com.openmapper.exceptions.entity.EntityNotFoundException;
import com.openmapper.model.operations.DmlOperation;

import static com.openmapper.model.operations.DmlOperation.SELECT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DaoMethod {

    String procedure() default "";

    DmlOperation operation() default SELECT;

    Class<? extends Throwable> translateNotFoundExceptionTo() default EntityNotFoundException.class;

    boolean returnKeys() default false;
}
