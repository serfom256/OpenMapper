package io.github.serfom256.benchmark.model.jimmer;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Entity
@Table(name = "author")
public interface Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String name();

    @Nullable
    String country();

    @OneToMany(mappedBy = "author")
    List<Book> books();
}
