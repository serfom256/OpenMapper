package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Model(primaryKey = "id")
@Getter
@Setter
@NoArgsConstructor
public class ProductOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    @Field(name = "price")
    private double price;

    @Field(name = "quantity")
    private int quantity;

    @Field(name = "description")
    private String description;
}
