package io.github.serfom256.core.lifecycle;

import io.github.serfom256.model.operations.DmlOperation;

/**
 * Called with an entity just before it is written, so values the application
 * maintains can be filled in one place instead of at every call site.
 *
 * <p>Declare an implementation as a bean and openMapper invokes it before the
 * entity's fields are read into statement parameters -- for a single write, for
 * every element of a batch, and for cascaded children.
 *
 * <p>{@link #type()} may name a base class or interface: every entity assignable
 * to it is passed, so one listener covers a whole hierarchy of audited entities.
 *
 * <pre>{@code
 * @Component
 * public class AuditListener implements EntityWriteListener<Auditable> {
 *
 *     public Class<Auditable> type() {
 *         return Auditable.class;
 *     }
 *
 *     public void beforeWrite(Auditable entity, DmlOperation operation) {
 *         Instant now = Instant.now();
 *         if (operation == DmlOperation.INSERT) {
 *             entity.setCreatedAt(now);
 *         }
 *         entity.setUpdatedAt(now);
 *     }
 * }
 * }</pre>
 *
 * <p>Reads never invoke listeners. The entity is the caller's own object, so any
 * change made here is visible to them after the call.
 */
public interface EntityWriteListener<T> {

    /** The entity type this listener applies to, subtypes included. */
    Class<T> type();

    /**
     * @param entity the instance about to be written
     * @param operation the statement being run, so inserts and updates can differ
     */
    void beforeWrite(T entity, DmlOperation operation);
}
