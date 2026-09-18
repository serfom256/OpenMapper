package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.OptimisticLockField;

@Model(primaryKey = "sku")
public class InventoryOM {

    @Field(name = "sku")
    private String sku;

    @Field(name = "quantity")
    private int quantity;

    @OptimisticLockField(name = "version", oldVersion = "old_version")
    private long version;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
