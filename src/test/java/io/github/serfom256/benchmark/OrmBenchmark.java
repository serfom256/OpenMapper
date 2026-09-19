package io.github.serfom256.benchmark;

import io.github.serfom256.benchmark.dao.AuthorDao;
import io.github.serfom256.benchmark.dao.ProductDao;
import io.github.serfom256.benchmark.dao.UserDao;
import io.github.serfom256.benchmark.model.jpa.*;
import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.config.OpenMapperConfig;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;
import org.openjdk.jmh.annotations.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import io.github.serfom256.benchmark.model.jimmer.AuthorFetcher;
import io.github.serfom256.benchmark.model.jimmer.BookFetcher;
import io.github.serfom256.benchmark.model.jimmer.ProductTable;
import io.github.serfom256.benchmark.model.jimmer.UserFetcher;
import io.github.serfom256.benchmark.model.jimmer.UserProfileFetcher;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(0)
public class OrmBenchmark {


    @State(Scope.Benchmark)
    public static class OpenMapperState {
        AnnotationConfigApplicationContext context;
        ProductDao productDao;
        UserDao userDao;
        AuthorDao authorDao;

        @Configuration
        @Profile("benchmark")
        @Import(OpenMapperConfig.class)
        @PropertySource("classpath:benchmark.properties")
        static class BenchmarkConfig {
            @Bean(destroyMethod = "")
            public DataSource dataSource() {
                return DatabaseSetup.getDataSource();
            }
        }

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();

            context = new AnnotationConfigApplicationContext();
            context.getEnvironment().setActiveProfiles("benchmark");
            context.register(BenchmarkConfig.class);
            context.refresh();

            productDao = context.getBean(ProductDao.class);
            userDao = context.getBean(UserDao.class);
            authorDao = context.getBean(AuthorDao.class);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
        }
    }


    @State(Scope.Benchmark)
    public static class JpaState {
        EntityManagerFactory emf;

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();

            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setDatabase(Database.POSTGRESQL);
            vendorAdapter.setGenerateDdl(false);
            vendorAdapter.setShowSql(false);

            LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
            emfb.setDataSource(DatabaseSetup.getDataSource());
            emfb.setPackagesToScan("io.github.serfom256.benchmark.model.jpa");
            emfb.setJpaVendorAdapter(vendorAdapter);

            Properties jpaProps = new Properties();
            jpaProps.put("hibernate.hbm2ddl.auto", "none");
            jpaProps.put("hibernate.format_sql", "false");
            jpaProps.put("hibernate.use_sql_comments", "false");
            jpaProps.put("hibernate.generate_statistics", "false");
            emfb.setJpaProperties(jpaProps);
            emfb.afterPropertiesSet();

            emf = emfb.getObject();
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (emf != null) emf.close();
        }

        EntityManager em() {
            return emf.createEntityManager();
        }
    }


    @State(Scope.Benchmark)
    public static class JimmerState {
        JSqlClient sqlClient;

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();
            DataSource ds = DatabaseSetup.getDataSource();
            sqlClient = JSqlClient.newBuilder()
                    .setDialect(new PostgresDialect())
                    .setConnectionManager(new org.babyfish.jimmer.sql.runtime.ConnectionManager() {
                        @Override
                        public <R> R execute(Function<Connection, R> block) {
                            try (Connection conn = ds.getConnection()) {
                                return block.apply(conn);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .build();
        }
    }


    @State(Scope.Thread)
    public static class ThreadState {
        Random random;

        @Setup(Level.Trial)
        public void setup() {
            random = new Random(Thread.currentThread().getId());
        }

        long randomProductId() { return 1L + random.nextInt(1000); }
        long randomUserId() { return 1L + random.nextInt(500); }
        long randomAuthorId() { return 1L + random.nextInt(100); }
    }


    static final int DELETE_SIMPLE_BATCH = 2_000;
    static final int DELETE_CASCADE_BATCH = 1_000;
    static final int BOOKS_PER_AUTHOR = 3;
    static final int BATCH_SIZE = 100;

    @State(Scope.Thread)
    public static class DeleteState {
        long[] simpleIds;
        long[] cascadeIds;
        int simpleIdx;
        int cascadeIdx;

        @Setup(Level.Iteration)
        public void seed() {
            simpleIds = DatabaseSetup.insertDeleteProducts(DELETE_SIMPLE_BATCH);
            cascadeIds = DatabaseSetup.insertDeleteAuthorsWithBooks(DELETE_CASCADE_BATCH, BOOKS_PER_AUTHOR);
            simpleIdx = 0;
            cascadeIdx = 0;
        }

        long nextSimpleId() {
            if (simpleIdx >= simpleIds.length) {
                throw new IllegalStateException(
                        "delete pool exhausted after %d ids: batchSize exceeds DELETE_SIMPLE_BATCH"
                                .formatted(simpleIds.length));
            }
            return simpleIds[simpleIdx++];
        }

        long nextCascadeId() {
            if (cascadeIdx >= cascadeIds.length) {
                throw new IllegalStateException(
                        "delete pool exhausted after %d ids: batchSize exceeds DELETE_CASCADE_BATCH"
                                .formatted(cascadeIds.length));
            }
            return cascadeIds[cascadeIdx++];
        }
    }


    @Benchmark
    public Object selectSimple_OpenMapper(OpenMapperState om, ThreadState ts) {
        return om.productDao.findProductById(ts.randomProductId());
    }

    @Benchmark
    public Object selectSimple_JPA(JpaState jpa, ThreadState ts) {
        EntityManager em = jpa.em();
        try {
            return em.find(ProductJpa.class, ts.randomProductId());
        } finally {
            em.close();
        }
    }

    @Benchmark
    public Object selectSimple_Jimmer(JimmerState jimmer, ThreadState ts) {
        return jimmer.sqlClient.getEntities().findById(
                io.github.serfom256.benchmark.model.jimmer.Product.class,
                ts.randomProductId()
        );
    }


    @Benchmark
    public Object selectOneToOne_OpenMapper(OpenMapperState om, ThreadState ts) {
        return om.userDao.findUserWithProfile(ts.randomUserId());
    }

    @Benchmark
    public Object selectOneToOne_JPA(JpaState jpa, ThreadState ts) {
        EntityManager em = jpa.em();
        try {
            return em.createQuery(
                            "SELECT u FROM UserJpa u LEFT JOIN FETCH u.profile WHERE u.id = :id",
                            UserJpa.class)
                    .setParameter("id", ts.randomUserId())
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Benchmark
    public Object selectOneToOne_Jimmer(JimmerState jimmer, ThreadState ts) {
        return jimmer.sqlClient.getEntities().findById(
                UserFetcher.$.username().email()
                        .profile(UserProfileFetcher.$.bio().avatarUrl()),
                ts.randomUserId()
        );
    }


    @Benchmark
    public Object selectOneToMany_OpenMapper(OpenMapperState om, ThreadState ts) {
        return om.authorDao.findAuthorWithBooks(ts.randomAuthorId());
    }

    @Benchmark
    public Object selectOneToMany_JPA(JpaState jpa, ThreadState ts) {
        EntityManager em = jpa.em();
        try {
            return em.createQuery(
                            "SELECT a FROM AuthorJpa a LEFT JOIN FETCH a.books WHERE a.id = :id",
                            AuthorJpa.class)
                    .setParameter("id", ts.randomAuthorId())
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Benchmark
    public Object selectOneToMany_Jimmer(JimmerState jimmer, ThreadState ts) {
        return jimmer.sqlClient.getEntities().findById(
                AuthorFetcher.$.name().country()
                        .books(BookFetcher.$.title().publishedYear().isbn()),
                ts.randomAuthorId()
        );
    }


    @Benchmark
    public Object selectBatch_OpenMapper(OpenMapperState om) {
        return om.productDao.findProducts(BATCH_SIZE);
    }

    @Benchmark
    public Object selectBatch_JPA(JpaState jpa) {
        EntityManager em = jpa.em();
        try {
            return em.createQuery("SELECT p FROM ProductJpa p ORDER BY p.id", ProductJpa.class)
                    .setMaxResults(BATCH_SIZE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Benchmark
    public Object selectBatch_Jimmer(JimmerState jimmer) {
        ProductTable product = ProductTable.$;
        return jimmer.sqlClient.createQuery(product)
                .orderBy(product.id())
                .select(product)
                .limit(BATCH_SIZE)
                .execute();
    }


    @Benchmark
    public void insertSimple_OpenMapper(OpenMapperState om) {
        ProductOM p = new ProductOM();
        p.setName("bench-insert");
        p.setPrice(19.99);
        p.setQuantity(42);
        p.setDescription("benchmark insert");
        om.productDao.insertProduct(p);
    }

    @Benchmark
    public void insertSimple_JPA(JpaState jpa) {
        EntityManager em = jpa.em();
        em.getTransaction().begin();
        ProductJpa p = new ProductJpa();
        p.setName("bench-insert");
        p.setPrice(19.99);
        p.setQuantity(42);
        p.setDescription("benchmark insert");
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    @Benchmark
    public void insertSimple_Jimmer(JimmerState jimmer) {
        jimmer.sqlClient.getEntities().save(
                io.github.serfom256.benchmark.model.jimmer.ProductDraft.$.produce(draft -> {
                    draft.setName("bench-insert");
                    draft.setPrice(19.99);
                    draft.setQuantity(42);
                    draft.setDescription("benchmark insert");
                })
        );
    }


    @Benchmark
    public void updateSimple_OpenMapper(OpenMapperState om, ThreadState ts) {
        ProductOM p = new ProductOM();
        p.setId(ts.randomProductId());
        p.setName("updated-name");
        p.setPrice(29.99);
        p.setQuantity(99);
        p.setDescription("updated desc");
        om.productDao.updateProduct(p);
    }

    @Benchmark
    public void updateSimple_JPA(JpaState jpa, ThreadState ts) {
        EntityManager em = jpa.em();
        em.getTransaction().begin();
        ProductJpa p = em.find(ProductJpa.class, ts.randomProductId());
        if (p != null) {
            p.setName("updated-name");
            p.setPrice(29.99);
            p.setQuantity(99);
            p.setDescription("updated desc");
        }
        em.getTransaction().commit();
        em.close();
    }

    @Benchmark
    public void updateSimple_Jimmer(JimmerState jimmer, ThreadState ts) {
        jimmer.sqlClient.getEntities().save(
                io.github.serfom256.benchmark.model.jimmer.ProductDraft.$.produce(draft -> {
                    draft.setId(ts.randomProductId());
                    draft.setName("updated-name");
                    draft.setPrice(29.99);
                    draft.setQuantity(99);
                    draft.setDescription("updated desc");
                })
        );
    }


    @Benchmark
    public void updateParent_OpenMapper(OpenMapperState om, ThreadState ts) {
        AuthorOM a = new AuthorOM();
        a.setId(ts.randomAuthorId());
        a.setName("updated-author");
        a.setCountry("DE");
        om.authorDao.updateAuthor(a);
    }

    @Benchmark
    public void updateParent_JPA(JpaState jpa, ThreadState ts) {
        EntityManager em = jpa.em();
        em.getTransaction().begin();
        AuthorJpa a = em.find(AuthorJpa.class, ts.randomAuthorId());
        if (a != null) {
            a.setName("updated-author");
            a.setCountry("DE");
        }
        em.getTransaction().commit();
        em.close();
    }

    @Benchmark
    public void updateParent_Jimmer(JimmerState jimmer, ThreadState ts) {
        jimmer.sqlClient.getEntities().save(
                io.github.serfom256.benchmark.model.jimmer.AuthorDraft.$.produce(draft -> {
                    draft.setId(ts.randomAuthorId());
                    draft.setName("updated-author");
                    draft.setCountry("DE");
                })
        );
    }


    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_SIMPLE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_SIMPLE_BATCH)
    @OperationsPerInvocation(DELETE_SIMPLE_BATCH)
    public void deleteSimple_OpenMapper(OpenMapperState om, DeleteState ds) {
        om.productDao.deleteProduct(ds.nextSimpleId());
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_SIMPLE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_SIMPLE_BATCH)
    @OperationsPerInvocation(DELETE_SIMPLE_BATCH)
    public void deleteSimple_JPA(JpaState jpa, DeleteState ds) {
        EntityManager em = jpa.em();
        em.getTransaction().begin();
        ProductJpa p = em.find(ProductJpa.class, ds.nextSimpleId());
        if (p != null) em.remove(p);
        em.getTransaction().commit();
        em.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_SIMPLE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_SIMPLE_BATCH)
    @OperationsPerInvocation(DELETE_SIMPLE_BATCH)
    public void deleteSimple_Jimmer(JimmerState jimmer, DeleteState ds) {
        jimmer.sqlClient.getEntities().delete(
                io.github.serfom256.benchmark.model.jimmer.Product.class,
                ds.nextSimpleId()
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_CASCADE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_CASCADE_BATCH)
    @OperationsPerInvocation(DELETE_CASCADE_BATCH)
    public void deleteCascade_OpenMapper(OpenMapperState om, DeleteState ds) {
        om.authorDao.deleteAuthor(ds.nextCascadeId());
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_CASCADE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_CASCADE_BATCH)
    @OperationsPerInvocation(DELETE_CASCADE_BATCH)
    public void deleteCascade_JPA(JpaState jpa, DeleteState ds) {
        EntityManager em = jpa.em();
        em.getTransaction().begin();
        AuthorJpa a = em.find(AuthorJpa.class, ds.nextCascadeId());
        if (a != null) em.remove(a);
        em.getTransaction().commit();
        em.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 3, batchSize = DELETE_CASCADE_BATCH)
    @Measurement(iterations = 5, batchSize = DELETE_CASCADE_BATCH)
    @OperationsPerInvocation(DELETE_CASCADE_BATCH)
    public void deleteCascade_Jimmer(JimmerState jimmer, DeleteState ds) {
        jimmer.sqlClient.getEntities().delete(
                io.github.serfom256.benchmark.model.jimmer.Author.class,
                ds.nextCascadeId()
        );
    }
}
