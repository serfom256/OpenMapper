package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface AuthorDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    AuthorOM findAuthorWithBooks(@Param(name = "id") long id);

    @DaoMethod(operation = DmlOperation.UPDATE)
    void updateAuthor(AuthorOM author);

    @DaoMethod(operation = DmlOperation.DELETE)
    void deleteAuthor(@Param(name = "id") long id);

    @DaoMethod(procedure = "insertAuthorReturningId", operation = DmlOperation.INSERT, returnKeys = true)
    long insertAuthorWithBooks(AuthorOM author);

    @DaoMethod(procedure = "findAllAuthorsWithBooks", operation = DmlOperation.SELECT)
    java.util.stream.Stream<AuthorOM> streamAuthorsWithBooks();

    @DaoMethod(procedure = "findAllAuthorsWithBooksUnordered", operation = DmlOperation.SELECT)
    java.util.stream.Stream<AuthorOM> streamAuthorsWithBooksUnordered();
}
