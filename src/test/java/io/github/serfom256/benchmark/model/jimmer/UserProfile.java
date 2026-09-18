package io.github.serfom256.benchmark.model.jimmer;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "user_profile")
public interface UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    @OneToOne
    User user();

    @Nullable
    String bio();

    @Nullable
    @Column(name = "avatar_url")
    String avatarUrl();
}
