package io.github.serfom256.model.page;

/**
 * The slice of a result a paged DAO method should return.
 *
 * <p>Pass one as a method parameter and openMapper binds two values into the
 * query: {@code limit} and {@code offset}. Write them into the procedure
 * yourself, so the ordering and the shape of the query stay in your SQL:
 *
 * <pre>{@code
 * findProductsPaged = {
 *     SELECT id, name FROM product ORDER BY id LIMIT [limit] OFFSET [offset]
 * }
 * }</pre>
 */
public final class PageRequest {

    public static final String LIMIT_PARAMETER = "limit";
    public static final String OFFSET_PARAMETER = "offset";

    private final int page;
    private final int size;

    private PageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative, got " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1, got " + size);
        }
        this.page = page;
        this.size = size;
    }

    /**
     * @param page zero-based page index
     * @param size rows per page, at least 1
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public int limit() {
        return size;
    }

    public long offset() {
        return (long) page * size;
    }

    public PageRequest next() {
        return new PageRequest(page + 1, size);
    }
}
