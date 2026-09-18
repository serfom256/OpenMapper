package io.github.serfom256.benchmark.model.jimmer;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "bench_user")
public interface User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String username();

    String email();

    @OneToOne(mappedBy = "user")
    @Nullable
    UserProfile profile();
}
