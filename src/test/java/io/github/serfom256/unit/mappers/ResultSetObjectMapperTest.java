package io.github.serfom256.unit.mappers;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultSetObjectMapperTest {

    private final ResultSetObjectMapper mapper =
            new ResultSetObjectMapper(new ResultSetGraphReader(new MappingPlanRegistry(null)));


    @Model(primaryKey = "id")
    public static class Product {
        @Field(name = "id") private long id;
        @Field(name = "name") private String name;
        public Product() {}
        public long getId() { return id; }
        public String getName() { return name; }
    }

    @Model(primaryKey = "id")
    public static class Author {
        @Field(name = "id") private long id;
        @Field(name = "name") private String name;
        @Joined(joinBy = "id", to = "authorId") private List<Book> books;
        public Author() {}
        public long getId() { return id; }
        public String getName() { return name; }
        public List<Book> getBooks() { return books; }
    }

    @Model(primaryKey = "bookId")
    public static class Book {
        @Field(name = "book_id") private long bookId;
        @Field(name = "author_id") private long authorId;
        @Field(name = "title") private String title;
        public Book() {}
        public long getBookId() { return bookId; }
        public String getTitle() { return title; }
    }


    @Test
    void singleEntity() throws SQLException {
        ResultSet rs = rows(
                new String[]{"id", "name"},
                new Object[][]{{1L, "Widget"}});

        Object result = mapper.extract(Product.class, Product.class, Product.class, rs);

        assertInstanceOf(Product.class, result);
        Product p = (Product) result;
        assertEquals(1L, p.getId());
        assertEquals("Widget", p.getName());
    }

    @Test
    void emptyResultSet_single_returnsNull() throws SQLException {
        ResultSet rs = rows(new String[]{"id", "name"}, new Object[][]{});
        assertNull(mapper.extract(Product.class, Product.class, Product.class, rs));
    }

    @Test
    void iterableReturn_listOfEntities() throws SQLException {
        ResultSet rs = rows(
                new String[]{"id", "name"},
                new Object[][]{{1L, "A"}, {2L, "B"}, {3L, "C"}});

        Object result = mapper.extract(Product.class, Product.class, List.class, rs);

        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals("A", ((Product) list.get(0)).getName());
        assertEquals("C", ((Product) list.get(2)).getName());
    }

    @Test
    void emptyResultSet_iterable_returnsEmptyList() throws SQLException {
        ResultSet rs = rows(new String[]{"id", "name"}, new Object[][]{});
        Object result = mapper.extract(Product.class, Product.class, List.class, rs);
        assertInstanceOf(List.class, result);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void oneToMany_collapsesRepeatedParentRows() throws SQLException {
        // a LEFT JOIN of one author with two books produces two rows
        ResultSet rs = rows(
                new String[]{"id", "name", "book_id", "author_id", "title"},
                new Object[][]{
                        {1L, "Tolkien", 10L, 1L, "Hobbit"},
                        {1L, "Tolkien", 11L, 1L, "LOTR"}
                });

        Object result = mapper.extract(Author.class, Author.class, Author.class, rs);

        Author author = (Author) result;
        assertEquals("Tolkien", author.getName());
        assertNotNull(author.getBooks());
        assertEquals(2, author.getBooks().size());
        assertEquals("Hobbit", author.getBooks().get(0).getTitle());
        assertEquals("LOTR", author.getBooks().get(1).getTitle());
    }

    @Test
    void oneToMany_authorWithNoBooks_getsEmptyCollection() throws SQLException {
        // LEFT JOIN with no matching child: book columns are SQL NULL
        ResultSet rs = rows(
                new String[]{"id", "name", "book_id", "author_id", "title"},
                new Object[][]{{1L, "Orphan", null, null, null}});

        Author author = (Author) mapper.extract(Author.class, Author.class, Author.class, rs);

        assertEquals("Orphan", author.getName());
        assertNotNull(author.getBooks());
        assertTrue(author.getBooks().isEmpty());
    }

    @Test
    void distinctAuthors_groupedById() throws SQLException {
        ResultSet rs = rows(
                new String[]{"id", "name", "book_id", "author_id", "title"},
                new Object[][]{
                        {1L, "A1", 10L, 1L, "b1"},
                        {2L, "A2", 20L, 2L, "b2"}
                });

        Object result = mapper.extract(Author.class, Author.class, List.class, rs);
        List<?> authors = (List<?>) result;
        assertEquals(2, authors.size());
        assertEquals(1, ((Author) authors.get(0)).getBooks().size());
        assertEquals(1, ((Author) authors.get(1)).getBooks().size());
    }

    @Test
    void joinKey_mismatchedRowsAreNotAttached() throws SQLException {
        // A cross join emits every author paired with every book. Only the rows
        // whose author_id matches the author's id are a real relationship; the
        // @Joined(joinBy = "id", to = "authorId") key is what tells them apart.
        ResultSet rs = rows(
                new String[]{"id", "name", "book_id", "author_id", "title"},
                new Object[][]{
                        {1L, "A1", 10L, 1L, "belongs to A1"},
                        {1L, "A1", 20L, 2L, "belongs to A2"},
                        {2L, "A2", 10L, 1L, "belongs to A1"},
                        {2L, "A2", 20L, 2L, "belongs to A2"}
                });

        List<?> authors = (List<?>) mapper.extract(Author.class, Author.class, List.class, rs);

        assertEquals(2, authors.size());
        Author a1 = (Author) authors.get(0);
        Author a2 = (Author) authors.get(1);
        assertEquals(1, a1.getBooks().size());
        assertEquals("belongs to A1", a1.getBooks().get(0).getTitle());
        assertEquals(1, a2.getBooks().size());
        assertEquals("belongs to A2", a2.getBooks().get(0).getTitle());
    }


    private ResultSet rows(String[] columns, Object[][] data) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData md = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(md);
        when(md.getColumnCount()).thenReturn(columns.length);

        int[] cursor = {-1};
        int[] lastColumnIdx = {-1};

        for (int i = 0; i < columns.length; i++) {
            final int idx = i;
            when(md.getColumnLabel(i + 1)).thenReturn(columns[i]);
            when(rs.getLong(columns[i])).thenAnswer(inv -> {
                lastColumnIdx[0] = idx;
                Object v = data[cursor[0]][idx];
                return v == null ? 0L : ((Number) v).longValue();
            });
            when(rs.getString(columns[i])).thenAnswer(inv -> {
                lastColumnIdx[0] = idx;
                return (String) data[cursor[0]][idx];
            });
            when(rs.getObject(columns[i])).thenAnswer(inv -> {
                lastColumnIdx[0] = idx;
                return data[cursor[0]][idx];
            });
        }
        when(rs.next()).thenAnswer(inv -> ++cursor[0] < data.length);
        // wasNull() reflects the value of the column read by the most recent getXxx call
        when(rs.wasNull()).thenAnswer(inv ->
                cursor[0] >= 0 && cursor[0] < data.length
                        && lastColumnIdx[0] >= 0
                        && data[cursor[0]][lastColumnIdx[0]] == null);
        return rs;
    }
}
