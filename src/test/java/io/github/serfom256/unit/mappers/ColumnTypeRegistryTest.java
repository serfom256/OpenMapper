package io.github.serfom256.unit.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.serfom256.exceptions.common.UnsupportedTypeException;
import io.github.serfom256.mappers.ColumnTypeConverter;
import io.github.serfom256.mappers.ColumnTypeRegistry;

class ColumnTypeRegistryTest {

    enum Status { ACTIVE, CLOSED }

    record Money(java.math.BigDecimal amount) {
    }

    static class MoneyConverter implements ColumnTypeConverter<Money> {
        public Class<Money> type() {
            return Money.class;
        }

        public Money fromColumn(ResultSet resultSet, String column) throws SQLException {
            java.math.BigDecimal amount = resultSet.getBigDecimal(column);
            return amount == null ? null : new Money(amount);
        }

        public Object toParameter(Money value) {
            return value == null ? null : value.amount();
        }
    }

    private final ColumnTypeRegistry registry = new ColumnTypeRegistry();

    @Test
    @DisplayName("java.time, UUID and byte[] are supported out of the box")
    void modernTypesAreSupported() {
        assertThat(registry.supports(LocalDate.class)).isTrue();
        assertThat(registry.supports(LocalDateTime.class)).isTrue();
        assertThat(registry.supports(Instant.class)).isTrue();
        assertThat(registry.supports(OffsetDateTime.class)).isTrue();
        assertThat(registry.supports(UUID.class)).isTrue();
        assertThat(registry.supports(byte[].class)).isTrue();
        assertThat(registry.supports(BigInteger.class)).isTrue();
    }

    @Test
    @DisplayName("any enum is supported without registration, read by name")
    void enumsAreSupported() throws SQLException {
        assertThat(registry.supports(Status.class)).isTrue();

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getString("status")).thenReturn("CLOSED");
        assertThat(registry.extractorFor(Status.class).extract(resultSet, "status"))
                .isEqualTo(Status.CLOSED);
    }

    @Test
    @DisplayName("an enum binds as its name")
    void enumBindsByName() {
        assertThat(registry.toParameter(Status.ACTIVE)).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("a SQL NULL enum column reads as null, not an exception")
    void nullEnumColumn() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getString("status")).thenReturn(null);
        assertThat(registry.extractorFor(Status.class).extract(resultSet, "status")).isNull();
    }

    @Test
    @DisplayName("an application converter handles a type openMapper does not know")
    void applicationConverter() throws SQLException {
        ColumnTypeRegistry withMoney = new ColumnTypeRegistry(List.of(new MoneyConverter()));
        assertThat(withMoney.supports(Money.class)).isTrue();

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getBigDecimal("price")).thenReturn(new java.math.BigDecimal("9.99"));
        assertThat(withMoney.extractorFor(Money.class).extract(resultSet, "price"))
                .isEqualTo(new Money(new java.math.BigDecimal("9.99")));
        assertThat(withMoney.toParameter(new Money(new java.math.BigDecimal("9.99"))))
                .isEqualTo(new java.math.BigDecimal("9.99"));
    }

    @Test
    @DisplayName("an application converter overrides a built-in type")
    void converterOverridesBuiltIn() throws SQLException {
        ColumnTypeConverter<Instant> epochMillis = new ColumnTypeConverter<>() {
            public Class<Instant> type() {
                return Instant.class;
            }

            public Instant fromColumn(ResultSet resultSet, String column) throws SQLException {
                return Instant.ofEpochMilli(resultSet.getLong(column));
            }
        };
        ColumnTypeRegistry overridden = new ColumnTypeRegistry(List.of(epochMillis));

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getLong("created")).thenReturn(1_000L);
        assertThat(overridden.extractorFor(Instant.class).extract(resultSet, "created"))
                .isEqualTo(Instant.ofEpochMilli(1_000L));
    }

    @Test
    @DisplayName("an unknown type still fails fast, naming the type")
    void unknownTypeFailsFast() {
        assertThatThrownBy(() -> registry.extractorFor(Thread.class))
                .isInstanceOf(UnsupportedTypeException.class)
                .hasMessageContaining("Thread");
    }
}
