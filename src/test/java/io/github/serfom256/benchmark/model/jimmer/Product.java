package io.github.serfom256.benchmark.model.jimmer;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "product")
public interface Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String name();

    double price();

    int quantity();

    @Nullable
    String description();
}
