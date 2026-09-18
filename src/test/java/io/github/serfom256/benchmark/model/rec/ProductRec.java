package io.github.serfom256.benchmark.model.rec;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;

/** Record counterpart of ProductOM, for comparing setter vs canonical-constructor mapping. */
@Model(primaryKey = "id")
public record ProductRec(
        @Field(name = "id") long id,
        @Field(name = "name") String name,
        @Field(name = "price") double price,
        @Field(name = "quantity") int quantity,
        @Field(name = "description") String description) {
}
