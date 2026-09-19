package io.github.serfom256.benchmark;

import io.github.serfom256.benchmark.dao.ProductDao;
import io.github.serfom256.benchmark.model.jpa.ProductJpa;
import io.github.serfom256.benchmark.repository.ProductJpaRepository;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(0)
public class ReadHeavyBenchmark {


    @State(Scope.Benchmark)
    public static class OpenMapperCachedState {
        AnnotationConfigApplicationContext context;
        ProductDao productDao;

        @Configuration
        @Profile("readheavy")
        @Import(OpenMapperConfig.class)
        @PropertySource("classpath:readheavy.properties")
        static class Cfg {
            @Bean(destroyMethod = "")
            public DataSource dataSource() {
                return DatabaseSetup.getDataSource();
            }
        }

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();
            context = new AnnotationConfigApplicationContext();
            context.getEnvironment().setActiveProfiles("readheavy");
            context.register(Cfg.class);
            context.refresh();
            productDao = context.getBean(ProductDao.class);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
        }
    }


    @State(Scope.Benchmark)
    public static class SpringDataJpaState {
        AnnotationConfigApplicationContext context;
        ProductJpaRepository repo;

        @Configuration
        @Profile("readheavy")
        @EnableJpaRepositories(basePackages = "io.github.serfom256.benchmark.repository")
        @EnableTransactionManagement
        static class Cfg {
            @Bean(destroyMethod = "")
            public DataSource dataSource() {
                return DatabaseSetup.getDataSource();
            }
            @Bean
            public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
                LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
                emf.setDataSource(ds);
                emf.setPackagesToScan("io.github.serfom256.benchmark.model.jpa");
                HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
                va.setDatabase(Database.POSTGRESQL);
                emf.setJpaVendorAdapter(va);
                Properties p = new Properties();
                p.put("hibernate.hbm2ddl.auto", "none");
                p.put("hibernate.show_sql", "false");
                emf.setJpaProperties(p);
                return emf;
            }
            @Bean
            public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
                return new JpaTransactionManager(emf);
            }
        }

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();
            context = new AnnotationConfigApplicationContext();
            context.getEnvironment().setActiveProfiles("readheavy");
            context.register(Cfg.class);
            context.refresh();
            repo = context.getBean(ProductJpaRepository.class);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
        }
    }


    @State(Scope.Benchmark)
    public static class RawJpaState {
        EntityManagerFactory emf;

        @Setup(Level.Trial)
        public void setup() {
            DatabaseSetup.initialize();
            HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
            va.setDatabase(Database.POSTGRESQL);
            LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
            emfb.setDataSource(DatabaseSetup.getDataSource());
            emfb.setPackagesToScan("io.github.serfom256.benchmark.model.jpa");
            emfb.setJpaVendorAdapter(va);
            Properties p = new Properties();
            p.put("hibernate.hbm2ddl.auto", "none");
            emfb.setJpaProperties(p);
            emfb.afterPropertiesSet();
            emf = emfb.getObject();
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
    public static class Ids {
        Random r;
        @Setup(Level.Trial)
        public void setup() {
            r = new Random(42);
        }
        long next() {
            return 1L + r.nextInt(1000);
        }
    }


    @Benchmark
    public Object findById_OpenMapperCached(OpenMapperCachedState s, Ids ids) {
        return s.productDao.findProductById(ids.next());
    }

    @Benchmark
    public Object findById_SpringDataJpa(SpringDataJpaState s, Ids ids) {
        return s.repo.findById(ids.next());
    }

    @Benchmark
    public Object findById_RawJpa(RawJpaState s, Ids ids) {
        EntityManager em = s.emf.createEntityManager();
        try {
            return em.find(ProductJpa.class, ids.next());
        } finally {
            em.close();
        }
    }

    @Benchmark
    public Object findById_Jimmer(JimmerState s, Ids ids) {
        return s.sqlClient.getEntities().findById(
                io.github.serfom256.benchmark.model.jimmer.Product.class, ids.next());
    }
}
