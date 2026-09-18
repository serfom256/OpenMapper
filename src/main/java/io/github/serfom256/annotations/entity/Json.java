package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @Field} whose database column holds JSON. On read the column
 * text is deserialized into the field's type; on write the field value is
 * serialized to a JSON string before binding. (de)serialization is delegated to
 * the active {@link io.github.serfom256.json.Serializer}.
 *
 * <p>For JSON/JSONB columns the SQL should cast the bind parameter where the
 * database requires it, e.g. {@code SET metadata = [metadata]::jsonb}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Json {
}
