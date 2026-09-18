package io.github.serfom256.core.lifecycle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.model.operations.DmlOperation;

public class EntityIdGenerators {

    private final List<IdGenerator<?>> generators;
    private final ModelFieldReader fieldReader;
    private final Map<Class<?>, Assignment> byType = new ConcurrentHashMap<>();

    private static final Assignment NONE = new Assignment(null, null, null);

    public EntityIdGenerators() {
        this(List.of(), new ModelFieldReader());
    }

    public EntityIdGenerators(Collection<IdGenerator<?>> generators, ModelFieldReader fieldReader) {
        this.generators = List.copyOf(generators);
        this.fieldReader = fieldReader;
    }

    public boolean isEmpty() {
        return generators.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void generateId(@Nullable Object entity, DmlOperation operation) {
        if (generators.isEmpty() || entity == null || operation != DmlOperation.INSERT) {
            return;
        }
        Assignment assignment = byType.computeIfAbsent(entity.getClass(), this::resolve);
        if (assignment.generator == null) {
            return;
        }
        if (!isAbsent(fieldReader.read(entity, assignment.field))) {
            return;
        }
        Object key = ((IdGenerator<Object>) assignment.generator).generate(entity);
        if (key == null) {
            return;
        }
        assign(entity, assignment, key);
    }

    private void assign(Object entity, Assignment assignment, Object key) {
        try {
            assignment.setter.invoke(entity, key);
        } catch (ClassCastException e) {
            throw new ModelValidationException(entity.getClass(),
                    "%s returned a %s for primary key '%s' of type %s"
                            .formatted(assignment.generator.getClass().getName(),
                                    key.getClass().getName(), assignment.field.getName(),
                                    assignment.field.getType().getName()));
        } catch (Throwable t) {
            throw new EntityFieldAccessException(t);
        }
    }

    private Assignment resolve(Class<?> entityType) {
        IdGenerator<?> generator = mostSpecificFor(entityType);
        if (generator == null) {
            return NONE;
        }
        Field field = primaryKeyField(entityType);
        return new Assignment(generator, field, setterFor(field));
    }

    private @Nullable IdGenerator<?> mostSpecificFor(Class<?> entityType) {
        IdGenerator<?> best = null;
        for (IdGenerator<?> generator : generators) {
            if (!generator.type().isAssignableFrom(entityType)) {
                continue;
            }
            if (best == null || best.type().isAssignableFrom(generator.type())) {
                best = generator;
            }
        }
        return best;
    }

    private Field primaryKeyField(Class<?> entityType) {
        Model model = entityType.getAnnotation(Model.class);
        if (model == null) {
            throw new ModelValidationException(entityType,
                    "an IdGenerator applies to it but it is not annotated @Model");
        }
        for (Field field : ObjectUtils.mappedFields(entityType)) {
            if (field.getName().equals(model.primaryKey())) {
                return field;
            }
        }
        throw new ModelValidationException(entityType,
                "no field maps the primary key '%s'".formatted(model.primaryKey()));
    }

    private MethodHandle setterFor(Field field) {
        try {
            return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup())
                    .unreflectSetter(field)
                    .asType(MethodType.methodType(void.class, Object.class, Object.class));
        } catch (IllegalAccessException e) {
            throw new EntityFieldAccessException(e);
        }
    }

    private boolean isAbsent(@Nullable Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Number number) {
            return number.longValue() == 0L;
        }
        return value instanceof CharSequence text && text.isEmpty();
    }

    private record Assignment(@Nullable IdGenerator<?> generator, @Nullable Field field,
                              @Nullable MethodHandle setter) {
    }
}
