package io.github.serfom256.exceptions.entity;

/**
 * Thrown when a query expected to match a row matched none, and the DAO method
 * opted into translation via
 * {@link io.github.serfom256.annotations.DaoMethod#translateNotFoundExceptionTo()}.
 *
 * <p>Unchecked, like every other openMapper exception, so a DAO method need not
 * declare it.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String reason) {
        super(reason);
    }

    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
