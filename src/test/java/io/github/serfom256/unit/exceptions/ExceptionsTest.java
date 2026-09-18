package io.github.serfom256.unit.exceptions;

import io.github.serfom256.exceptions.common.FunctionNotFoundException;
import io.github.serfom256.exceptions.common.UnsupportedTypeException;
import io.github.serfom256.exceptions.entity.*;
import io.github.serfom256.exceptions.fsql.*;
import io.github.serfom256.exceptions.internal.*;
import io.github.serfom256.parser.model.CharLocation;
import io.github.serfom256.parser.model.SpanLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void functionNotFoundException() {
        FunctionNotFoundException ex = new FunctionNotFoundException("findById");
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("findById"));
    }

    @Test
    void unsupportedTypeException() {
        UnsupportedTypeException ex = new UnsupportedTypeException("java.util.UUID");
        assertNotNull(ex.getMessage());
    }

    @Test
    void entityNotFoundException() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");
        assertNotNull(ex.getMessage());
    }

    @Test
    void entityFieldAccessException() {
        EntityFieldAccessException ex = new EntityFieldAccessException(new IllegalAccessException("test"));
        assertNotNull(ex.getCause());
    }

    @Test
    void entityMappingException() {
        EntityMappingException ex = new EntityMappingException(new RuntimeException("mapping error"));
        assertNotNull(ex.getCause());
    }

    @Test
    void objectCreationException() {
        ObjectCreationException ex = new ObjectCreationException(new ReflectiveOperationException("test"));
        assertNotNull(ex.getCause());
    }

    @Test
    void modelValidationException() {
        ModelValidationException ex = new ModelValidationException(String.class, "bad model");
        assertNotNull(ex.getMessage());
    }

    @Test
    void invalidFileFormatException() {
        InvalidFileFormatException ex = new InvalidFileFormatException("test.txt");
        assertNotNull(ex.getMessage());
    }

    @Test
    void fsqlParsingException() {
        SpanLocation span = new SpanLocation(
                new CharLocation(1, 5, 5),
                new CharLocation(1, 10, 10));
        FsqlParsingException ex = new FsqlParsingException("test.fsql", span);
        assertNotNull(ex.getMessage());
    }

    @Test
    void invalidDeclarationException() {
        InvalidDeclarationException ex = new InvalidDeclarationException("myProc", "bad syntax");
        assertNotNull(ex.getMessage());
    }

    @Test
    void invalidResultCountException() {
        InvalidResultCountException ex = new InvalidResultCountException("expected 1 got 3");
        assertNotNull(ex.getMessage());
    }

    @Test
    void tooManyResultsFoundException() {
        TooManyResultsFoundException ex = new TooManyResultsFoundException("too many");
        assertNotNull(ex.getMessage());
    }

    @Test
    void resultSetExtractionException() {
        ResultSetExtractionException ex = new ResultSetExtractionException(new java.sql.SQLException("fail"));
        assertNotNull(ex.getCause());
    }

    @Test
    void optimisticLockException_noArg() {
        OptimisticLockException ex = new OptimisticLockException();
        assertNotNull(ex);
    }

    @Test
    void optimisticLockException_withMessage() {
        OptimisticLockException ex = new OptimisticLockException("retries exceeded");
        assertEquals("retries exceeded", ex.getMessage());
    }

    @Test
    void queryExecutionError() {
        QueryExecutionError ex = new QueryExecutionError(new java.sql.SQLException("timeout"));
        assertNotNull(ex.getCause());
    }
}
