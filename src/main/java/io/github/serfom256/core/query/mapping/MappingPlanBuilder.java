package io.github.serfom256.core.query.mapping;

import io.github.serfom256.annotations.entity.EnumType;
import io.github.serfom256.annotations.entity.Inheritance;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Json;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Nested;
import io.github.serfom256.annotations.entity.OptimisticLockField;
import io.github.serfom256.annotations.entity.Subtype;
import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.json.Serializer;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import io.github.serfom256.mappers.ResultSetUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a {@link MappingPlan} for an entity type by reading its annotations
 * once and resolving every field/constructor into a method handle. The plan
 * never references the (mutable, shared) model metadata, so building it has no
 * side effects on other consumers.
 */
final class MappingPlanBuilder {

    private final Serializer jsonCodec;
    private final ColumnTypeRegistry columnTypes;

    MappingPlanBuilder(Serializer jsonCodec, ColumnTypeRegistry columnTypes) {
        this.jsonCodec = jsonCodec;
        this.columnTypes = columnTypes;
    }

    MappingPlan build(Class<?> type) {
        Model model = type.getAnnotation(Model.class);
        String primaryKeyField = model != null ? model.primaryKey() : null;
        boolean canonical = !ReflectiveAccessors.hasNoArgConstructor(type);

        List<ScalarBinding> scalars = new ArrayList<>();
        List<NestedBinding> nested = new ArrayList<>();
        List<AssociationBinding> associations = new ArrayList<>();
        ScalarBinding primaryKey = null;

        for (Field field : ObjectUtils.mappedFields(type)) {
            io.github.serfom256.annotations.entity.Field scalarAnnotation =
                    field.getAnnotation(io.github.serfom256.annotations.entity.Field.class);
            OptimisticLockField lockAnnotation = field.getAnnotation(OptimisticLockField.class);
            Joined joinedAnnotation = field.getAnnotation(Joined.class);
            Nested nestedAnnotation = field.getAnnotation(Nested.class);

            if (scalarAnnotation != null || lockAnnotation != null) {
                String column = columnName(field,
                        scalarAnnotation != null ? scalarAnnotation.name() : lockAnnotation.name());
                ResultSetUtils.ColumnExtractor extractor = field.isAnnotationPresent(Json.class)
                        ? jsonExtractor(field)
                        : columnTypes.extractorFor(field.getType(), enumTypeOf(scalarAnnotation));
                ScalarBinding binding = new ScalarBinding(field.getName(), column,
                        canonical ? null : ReflectiveAccessors.setter(field), extractor,
                        field.getType());
                scalars.add(binding);
                if (field.getName().equals(primaryKeyField)) {
                    primaryKey = binding;
                }
            } else if (joinedAnnotation != null) {
                requireMutable(type, canonical, "@Joined");
                associations.add(buildAssociation(field));
            } else if (nestedAnnotation != null) {
                requireMutable(type, canonical, "@Nested");
                nested.add(buildNested(field));
            }
        }

        if (primaryKey == null) {
            throw new ModelValidationException(type,
                    "no @Field maps the primary key '" + primaryKeyField + "'");
        }

        Inheritance inheritance = type.getDeclaredAnnotation(Inheritance.class);
        String discriminator = inheritance != null ? inheritance.discriminator() : null;
        Map<String, Class<?>> subtypes = subtypesOf(type, inheritance);

        if (!canonical) {
            return new MappingPlan(type, ReflectiveAccessors.constructor(type), false,
                    primaryKey, scalars, nested, associations, discriminator, subtypes);
        }
        return new MappingPlan(type, canonicalConstructor(type, scalars), true,
                primaryKey, scalars, nested, associations, discriminator, subtypes);
    }

    private Map<String, Class<?>> subtypesOf(Class<?> type, Inheritance inheritance) {
        Subtype[] declared = type.getDeclaredAnnotationsByType(Subtype.class);
        if (declared.length == 0) {
            return Map.of();
        }
        if (inheritance == null) {
            throw new ModelValidationException(type, "@Subtype needs @Inheritance to name the discriminator column");
        }
        Map<String, Class<?>> subtypes = new LinkedHashMap<>();
        for (Subtype subtype : declared) {
            if (!type.isAssignableFrom(subtype.type())) {
                throw new ModelValidationException(type,
                        "@Subtype(\"%s\") points at %s, which does not extend it"
                                .formatted(subtype.value(), subtype.type().getName()));
            }
            Class<?> clash = subtypes.put(subtype.value(), subtype.type());
            if (clash != null) {
                throw new ModelValidationException(type,
                        "discriminator value '%s' is claimed by both %s and %s"
                                .formatted(subtype.value(), clash.getName(), subtype.type().getName()));
            }
        }
        return subtypes;
    }

    private void requireMutable(Class<?> type, boolean canonical, String annotation) {
        if (canonical) {
            throw new ModelValidationException(type,
                    annotation + " needs a mutable type: wiring it means setting a field after"
                            + " construction, which an immutable type does not allow");
        }
    }

    private java.lang.invoke.MethodHandle canonicalConstructor(Class<?> type, List<ScalarBinding> scalars) {
        Class<?>[] fieldTypes = new Class<?>[scalars.size()];
        for (int i = 0; i < fieldTypes.length; i++) {
            fieldTypes[i] = scalars.get(i).fieldType();
        }
        java.lang.reflect.Constructor<?> constructor =
                ReflectiveAccessors.canonicalConstructor(type, fieldTypes);
        if (constructor == null) {
            throw new ModelValidationException(type,
                    "no no-argument constructor, and no constructor matching the mapped fields in"
                            + " declaration order; every field of an immutable type must be mapped");
        }
        return ReflectiveAccessors.spreadingConstructor(constructor);
    }

    private AssociationBinding buildAssociation(Field field) {
        Class<?> targetType = ObjectUtils.getInnerClassType(field.getType(), field.getGenericType());
        boolean collection = ObjectUtils.isIterable(field.getType());
        boolean set = Set.class.isAssignableFrom(field.getType());
        Joined joined = field.getAnnotation(Joined.class);
        return new AssociationBinding(
                ReflectiveAccessors.getter(field),
                ReflectiveAccessors.setter(field),
                targetType, collection, set,
                joined.joinBy(), joined.to());
    }

    private NestedBinding buildNested(Field field) {
        Class<?> nestedType = field.getType();
        List<ScalarBinding> scalars = new ArrayList<>();
        for (Field nestedField : ObjectUtils.mappedFields(nestedType)) {
            io.github.serfom256.annotations.entity.Field annotation =
                    nestedField.getAnnotation(io.github.serfom256.annotations.entity.Field.class);
            if (annotation != null) {
                scalars.add(new ScalarBinding(nestedField.getName(),
                        columnName(nestedField, annotation.name()),
                        ReflectiveAccessors.setter(nestedField),
                        columnTypes.extractorFor(nestedField.getType(), enumTypeOf(annotation)),
                        nestedField.getType()));
            }
        }
        return new NestedBinding(ReflectiveAccessors.setter(field),
                ReflectiveAccessors.constructor(nestedType), scalars);
    }

    private ResultSetUtils.ColumnExtractor jsonExtractor(Field field) {
        if (jsonCodec == null) {
            throw new ModelValidationException(field.getDeclaringClass(),
                    "field '" + field.getName() + "' is @Json but no JsonCodec is available "
                            + "(add Jackson to the classpath or define a JsonCodec bean)");
        }
        final Type targetType = field.getGenericType();
        return (resultSet, column) -> {
            String text = resultSet.getString(column);
            return text == null ? null : jsonCodec.deserialize(text, targetType);
        };
    }

    private EnumType enumTypeOf(io.github.serfom256.annotations.entity.Field annotation) {
        return annotation == null ? EnumType.NAME : annotation.enumType();
    }

    private String columnName(Field field, String annotationName) {
        return annotationName == null || annotationName.isEmpty() ? field.getName() : annotationName;
    }
}
