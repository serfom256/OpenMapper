package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Model(primaryKey = "bookId")
@Getter
@Setter
@NoArgsConstructor
public class BookOM {

    @Field(name = "book_id")
    private long bookId;

    @Field(name = "title")
    private String title;

    @Field(name = "author_id")
    private long authorId;

    @Field(name = "published_year")
    private int publishedYear;

    @Field(name = "isbn")
    private String isbn;
}
