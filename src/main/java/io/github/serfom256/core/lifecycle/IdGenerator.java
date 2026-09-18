package io.github.serfom256.core.lifecycle;

import org.jspecify.annotations.Nullable;

/**
 * Supplies the primary key for an entity that the database does not generate.
 *
 * <p>Declare an implementation as a bean and openMapper calls it immediately
 * before an {@code INSERT}, assigning the result to the field named by
 * {@link io.github.serfom256.annotations.entity.Model#primaryKey()} -- for a single
 * write, for every element of a batch, and for cascaded children.
 *
 * <p>It runs only when the key is still unset: {@code null}, a zero number, or
 * an empty string. A caller that assigned the key itself keeps it.
 *
 * <pre>{@code
 * @Component
 * public class OrderIdGenerator implements IdGenerator<Order> {
 *
 *     private final SequenceAllocator allocator;
 *
 *     public Class<Order> type() {
 *         return Order.class;
 *     }
 *
 *     public Object generate(Order order) {
 *         return allocator.next("ORDER");
 *     }
 * }
 * }</pre>
 *
 * <p>{@link #type()} may name a base class or interface: every entity assignable
 * to it is passed, so one generator covers a whole hierarchy. Where two
 * generators both apply, the more specific {@code type()} wins; between two
 * equally specific ones, declare an order with {@code @Order}.
 *
 * <p>This is the alternative to {@code @DaoMethod(returnKeys = true)}, which
 * reads a key the database produced. Use one or the other, not both: a key
 * assigned here is sent with the insert, so there is nothing to read back.
 *
 * @param <T> the entity type this generator applies to, subtypes included
 */
public interface IdGenerator<T> {

    /** The entity type this generator applies to, subtypes included. */
    Class<T> type();

    /**
     * @param entity the instance about to be inserted, with every other field set
     * @return the key to assign, or {@code null} to leave the key alone
     */
    @Nullable Object generate(T entity);
}
