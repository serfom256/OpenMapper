package io.github.serfom256.benchmark.model.om;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Model(primaryKey = "id")
@Getter
@Setter
@NoArgsConstructor
public class AuthorOM {

    @Field(name = "id")
    private long id;

    @Field(name = "name")
    private String name;

    @Field(name = "country")
    private String country;

    @Joined(joinBy = "id", to = "authorId", saveProcedure = "insertBookForAuthor")
    private List<BookOM> books;
}
