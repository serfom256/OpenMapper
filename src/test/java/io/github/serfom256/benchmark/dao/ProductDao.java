package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.model.operations.DmlOperation;

import java.util.List;

@DaoLayer
public interface ProductDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    ProductOM findProductById(@Param(name = "id") long id);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<ProductOM> findProducts(@Param(name = "limit") int limit);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<ProductOM> findByName(@Param(name = "name") String name);

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertProduct(ProductOM product);

    @DaoMethod(operation = DmlOperation.UPDATE)
    void updateProduct(ProductOM product);

    @DaoMethod(operation = DmlOperation.DELETE)
    void deleteProduct(@Param(name = "id") long id);

    @DaoMethod(procedure = "insertProduct", operation = DmlOperation.INSERT)
    int insertProducts(List<ProductOM> products);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<ProductOM> findProductsByIds(@Param(name = "ids") List<Long> ids);

    @DaoMethod(procedure = "findByName", operation = DmlOperation.SELECT, fetchSize = 50)
    java.util.stream.Stream<ProductOM> streamByName(@Param(name = "name") String name);

    @DaoMethod(procedure = "findProductsPaged", countProcedure = "countProductsByName",
            operation = DmlOperation.SELECT)
    io.github.serfom256.model.page.Page<ProductOM> findPagedByName(@Param(name = "name") String name,
            io.github.serfom256.model.page.PageRequest pageRequest);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<ProductOM> findProductsExcludingIds(@Param(name = "description") String description,
                                             @Param(name = "ids") List<Long> ids);
}
