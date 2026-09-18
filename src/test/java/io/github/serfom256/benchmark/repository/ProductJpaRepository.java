package io.github.serfom256.benchmark.repository;

import io.github.serfom256.benchmark.model.jpa.ProductJpa;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Idiomatic Spring Data JPA repository — mirrors how the user's OpenMapperTest
 * project uses JpaRepository. Each CRUD call goes through a repository proxy and
 * a @Transactional interceptor, which is the per-call overhead being measured.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpa, Long> {
}
