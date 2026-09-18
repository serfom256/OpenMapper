package io.github.serfom256.model.page;

import java.util.List;

/**
 * One page of results together with the total number of rows the query matches.
 *
 * <p>Returned by a DAO method declared as {@code Page<E>}. The total comes from
 * the counting procedure named by
 * {@link io.github.serfom256.annotations.DaoMethod#countProcedure()}, which runs as a
 * second statement.
 */
public final class Page<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;

    public Page(List<T> content, int page, int size, long totalElements) {
        this.content = List.copyOf(content);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
    }

    public List<T> content() {
        return content;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public long totalElements() {
        return totalElements;
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalElements;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
