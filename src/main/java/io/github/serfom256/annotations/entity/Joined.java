package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as an association populated from the same result set: a single
 * related {@link Model}, or a {@link java.util.Collection} of them.
 *
 * <p>The join is expressed entirely in the SQL procedure -- openMapper does not
 * generate one. Write the query so each parent row repeats per child row; the
 * rows are then collapsed back into one parent using the parent's
 * {@link Model#primaryKey()}, which must therefore be set.
 *
 * <p>{@link #joinBy()} and {@link #to()} name the two sides of the relationship,
 * and a child is attached to a parent only when those values agree on the row.
 * Without them a child would be attached purely because it shares a row, which
 * silently produces wrong graphs whenever the query emits more combinations than
 * the relationship allows -- a cross join, or a second association fanning out.
 *
 * <pre>{@code
 * @Model(primaryKey = "id")
 * public class Author {
 *     @Field(name = "id")   private long id;
 *     @Joined(joinBy = "id", to = "authorId")
 *     private List<Book> books;
 * }
 *
 * @Model(primaryKey = "bookId")
 * public class Book {
 *     @Field(name = "book_id")   private long bookId;
 *     @Field(name = "author_id") private long authorId;   // <- referenced by to()
 * }
 * }</pre>
 *
 * <p>Collection-valued fields may be declared as {@link java.util.List} or
 * {@link java.util.Set}; the element type is taken from the generic signature. A
 * parent with no children gets an empty collection, never {@code null}.
 *
 * @see Nested for a nested object sharing a single row
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Joined {

    /**
     * Name of the <em>Java field</em> on the declaring entity holding this side
     * of the join key -- usually the one mapping its primary key column.
     *
     * <p>The field must be mapped by a {@link Field}. When it is not, or when
     * its column is not selected by the query, the key check is skipped and the
     * association falls back to row co-occurrence.
     *
     * @return the owning entity's join-key field name
     */
    String joinBy();

    /**
     * The other side of the relationship.
     *
     * <p>For a one-to-many this is the <em>Java field</em> on the target entity
     * holding the foreign key, resolved to its column. The key check then keeps a
     * child only when the two values agree on the row.
     *
     * <p>For a many-to-many there is no such field, because the foreign key lives
     * in a link table. Name the link table's column instead: it will not resolve
     * against the target entity, so the key check is skipped and the rows the SQL
     * join produced are taken as given -- and {@link #saveProcedure()} receives
     * the parent key under that name, ready to insert into the link table.
     *
     * <pre>{@code
     * @Joined(joinBy = "id", to = "student_id", saveProcedure = "linkStudentCourse")
     * private List<Course> courses;
     * }</pre>
     *
     * <p>Take care that the name does not accidentally match a mapped field on the
     * target entity: the key check would then compare unrelated values and quietly
     * drop rows.
     *
     * @return the target entity's foreign-key field, or the link table's column
     */
    String to();

    /**
     * Procedure that writes one element of this association, enabling cascade.
     *
     * <p>Left empty the association is read-only: it is populated when the parent
     * is read and ignored when the parent is written. Naming a procedure makes
     * openMapper run it for every element after the parent's own statement
     * succeeds, as a single batch.
     *
     * <p>The parent's key is bound under the name given by {@link #to()}, so the
     * procedure can reference it without the element carrying it:
     *
     * <pre>{@code
     * insertBook = {
     *     INSERT INTO book (title, author_id) VALUES ([title], [authorId])
     * }
     * }</pre>
     *
     * <p>Elements are inserted or updated as the procedure dictates. Rows present
     * in the database but absent from the collection are left alone; removing
     * them is a separate, explicit call.
     *
     * @return the procedure that writes one element, or {@code ""} for a
     *         read-only association
     */
    String saveProcedure() default "";
}
