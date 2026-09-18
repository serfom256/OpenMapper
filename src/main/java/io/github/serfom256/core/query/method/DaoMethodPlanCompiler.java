package io.github.serfom256.core.query.method;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.serfom256.annotations.Cached;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.annotations.entity.Dto;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Sql;
import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.exceptions.entity.NoTranslationException;
import io.github.serfom256.exceptions.fsql.InvalidDeclarationException;
import io.github.serfom256.model.page.Page;
import io.github.serfom256.model.page.PageRequest;
import io.github.serfom256.model.operations.DmlOperation;

final class DaoMethodPlanCompiler {

    private final ResultSetHandlerStrategy handlerStrategy;

    DaoMethodPlanCompiler(ResultSetHandlerStrategy handlerStrategy) {
        this.handlerStrategy = handlerStrategy;
    }

    DaoMethodPlan compile(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        boolean returnsOptional = method.getReturnType() == Optional.class;
        boolean returnsStream = method.getReturnType() == Stream.class;
        boolean returnsPage = method.getReturnType() == Page.class;
        if (returnsPage) {
            requirePageSupport(method, daoMethod);
        }

        return DaoMethodPlan.builder()
                .procedure(procedureName(method, daoMethod))
                .operation(daoMethod != null ? daoMethod.operation() : DmlOperation.SELECT)
                .returnGeneratedKeys(daoMethod != null && daoMethod.returnKeys())
                .notFoundException(notFoundException(daoMethod, method))
                .cached(method.getAnnotation(Cached.class) != null)
                .handler(handlerStrategy.getHandlerByMethodReturnType(
                        handledReturnType(method, returnsOptional)))
                .genericReturnType(genericReturnType(method, returnsOptional))
                .returnsOptional(returnsOptional)
                .returnsStream(returnsStream)
                .returnsPage(returnsPage)
                .countProcedure(daoMethod != null ? daoMethod.countProcedure() : "")
                .sqlParameterIndex(sqlParameterIndex(method))
                .parameters(parameterBindings(method))
                .batchParameterIndex(batchParameterIndex(method, daoMethod))
                .batchElement(batchElement(method, daoMethod))
                .fetchSize(daoMethod != null ? daoMethod.fetchSize() : 0)
                .queryTimeoutSeconds(daoMethod != null ? daoMethod.queryTimeoutSeconds() : 0)
                .build();
    }

    private String procedureName(Method method, DaoMethod daoMethod) {
        if (daoMethod != null && !daoMethod.procedure().isEmpty()) {
            return daoMethod.procedure();
        }
        return method.getName();
    }

    private Class<? extends Throwable> notFoundException(DaoMethod daoMethod, Method method) {
        if (daoMethod == null || daoMethod.translateNotFoundExceptionTo() == NoTranslationException.class) {
            return null;
        }
        if (expressesAbsenceItself(method.getReturnType())) {
            return null;
        }
        return daoMethod.translateNotFoundExceptionTo();
    }

    private boolean expressesAbsenceItself(Class<?> returnType) {
        return returnType == Optional.class
                || returnType == void.class
                || returnType == Void.class
                || returnType.isArray()
                || Collection.class.isAssignableFrom(returnType)
                || Iterable.class.isAssignableFrom(returnType);
    }

    private void requirePageSupport(Method method, DaoMethod daoMethod) {
        if (daoMethod == null || daoMethod.countProcedure().isEmpty()) {
            throw new InvalidDeclarationException(method.getName(),
                    "a method returning Page must declare @DaoMethod(countProcedure = \"...\")");
        }
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getType() == PageRequest.class) {
                return;
            }
        }
        throw new InvalidDeclarationException(method.getName(),
                "a method returning Page must take a PageRequest parameter");
    }

    private Class<?> handledReturnType(Method method, boolean returnsOptional) {
        if (method.getReturnType() == Page.class) {
            return List.class;
        }
        if (method.getReturnType() == Stream.class) {
            return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        if (returnsOptional) {
            return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        return method.getReturnType();
    }

    private Type genericReturnType(Method method, boolean returnsOptional) {
        if (method.getReturnType() == Page.class) {
            return method.getGenericReturnType();
        }
        if (method.getReturnType() == Stream.class) {
            return ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        if (returnsOptional) {
            return ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        return method.getGenericReturnType();
    }

    private int sqlParameterIndex(Method method) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(Sql.class) != null) {
                return i;
            }
        }
        return -1;
    }

    private int batchParameterIndex(Method method, DaoMethod daoMethod) {
        if (operationOf(daoMethod) == DmlOperation.SELECT) {
            return -1;
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (ObjectUtils.isIterable(parameters[i].getType()) && isEntityCollection(parameters[i])) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEntityCollection(Parameter parameter) {
        Class<?> elementType = ObjectUtils.getInnerClassType(parameter.getType(),
                parameter.getParameterizedType());
        return elementType.getAnnotation(Model.class) != null
                || elementType.getAnnotation(Dto.class) != null;
    }

    private ParameterBinding batchElement(Method method, DaoMethod daoMethod) {
        int index = batchParameterIndex(method, daoMethod);
        if (index < 0) {
            return null;
        }
        Parameter parameter = method.getParameters()[index];
        Class<?> elementType = ObjectUtils.getInnerClassType(parameter.getType(),
                parameter.getParameterizedType());
        if (elementType.getAnnotation(Model.class) != null) {
            return new ParameterBinding(parameter.getName(), ParameterBinding.Kind.MODEL);
        }
        if (elementType.getAnnotation(Dto.class) != null) {
            return new ParameterBinding(parameter.getName(), ParameterBinding.Kind.DTO);
        }
        Param param = parameter.getAnnotation(Param.class);
        boolean named = param != null && !param.name().isEmpty();
        return new ParameterBinding(named ? param.name() : parameter.getName(),
                ParameterBinding.Kind.PARAM);
    }

    private DmlOperation operationOf(DaoMethod daoMethod) {
        return daoMethod != null ? daoMethod.operation() : DmlOperation.SELECT;
    }

    private ParameterBinding[] parameterBindings(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            bindings[i] = binding(parameters[i]);
        }
        return bindings;
    }

    private ParameterBinding binding(Parameter parameter) {
        if (parameter.getType() == PageRequest.class) {
            return new ParameterBinding(parameter.getName(), ParameterBinding.Kind.PAGE_REQUEST);
        }
        if (parameter.getType().getAnnotation(Model.class) != null) {
            return new ParameterBinding(parameter.getName(), ParameterBinding.Kind.MODEL);
        }
        if (parameter.getType().getAnnotation(Dto.class) != null) {
            return new ParameterBinding(parameter.getName(), ParameterBinding.Kind.DTO);
        }
        Param param = parameter.getAnnotation(Param.class);
        boolean named = param != null && !param.name().isEmpty();
        return new ParameterBinding(named ? param.name() : parameter.getName(),
                ParameterBinding.Kind.PARAM);
    }
}
