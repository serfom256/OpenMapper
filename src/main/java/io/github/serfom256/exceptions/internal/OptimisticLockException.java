package io.github.serfom256.exceptions.internal;

public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException() {
        super();
    }

    public OptimisticLockException(String message) {
        super(message);
    }
}
