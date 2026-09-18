package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;

public class SkuItemOM extends CatalogItemOM {

    @Field(name = "sku_code")
    private String skuCode;

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }
}
