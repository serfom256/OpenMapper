package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Inheritance;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Subtype;

@Model(primaryKey = "id")
@Inheritance(discriminator = "item_type")
@Subtype(value = "BUNDLE", type = BundleItemOM.class)
@Subtype(value = "SKU", type = SkuItemOM.class)
public class CatalogItemOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
