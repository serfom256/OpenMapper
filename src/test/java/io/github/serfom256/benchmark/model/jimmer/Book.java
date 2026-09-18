package io.github.serfom256.benchmark.model.jimmer;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "book")
public interface Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String title();

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    Author author();

    @Nullable
    @Column(name = "published_year")
    Integer publishedYear();

    @Nullable
    String isbn();
}
