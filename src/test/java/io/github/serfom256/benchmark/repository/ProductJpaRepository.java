package io.github.serfom256.benchmark.repository;

import io.github.serfom256.benchmark.model.jpa.ProductJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpa, Long> {
}
