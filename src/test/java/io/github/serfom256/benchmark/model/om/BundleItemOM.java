package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;

public class BundleItemOM extends CatalogItemOM {

    @Field(name = "bundle_size")
    private int bundleSize;

    public int getBundleSize() {
        return bundleSize;
    }

    public void setBundleSize(int bundleSize) {
        this.bundleSize = bundleSize;
    }
}
