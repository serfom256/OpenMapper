package io.github.serfom256.benchmark.model.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
public class BookJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorJpa author;

    @Column(name = "published_year")
    private Integer publishedYear;

    private String isbn;
}
