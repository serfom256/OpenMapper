package io.github.serfom256.mappers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.annotations.entity.EnumType;
import io.github.serfom256.exceptions.common.UnsupportedTypeException;
import io.github.serfom256.mappers.ResultSetUtils.ColumnExtractor;
import io.github.serfom256.mappers.ResultSetUtils.PositionalExtractor;

public class ColumnTypeRegistry {

    private final Map<Class<?>, ColumnTypeConverter<?>> converters = new HashMap<>();
    private final Map<Class<?>, ColumnExtractor> resolved = new ConcurrentHashMap<>();
    private final Map<Class<?>, ColumnExtractor> builtIn = new HashMap<>(48);
    private final Map<Class<?>, PositionalExtractor> builtInByIndex = new HashMap<>(48);

    public ColumnTypeRegistry() {
        this(List.of());
    }

    public ColumnTypeRegistry(Collection<ColumnTypeConverter<?>> applicationConverters) {
        registerBuiltIns();
        for (ColumnTypeConverter<?> converter : applicationConverters) {
            converters.put(converter.type(), converter);
        }
    }

    public ColumnExtractor extractorFor(Class<?> type) {
        ColumnExtractor extractor = resolved.computeIfAbsent(type, this::resolve);
        if (extractor == NONE) {
            throw new UnsupportedTypeException(type.getTypeName());
        }
        return extractor;
    }

    public ColumnExtractor extractorFor(Class<?> type, EnumType enumType) {
        if (enumType == EnumType.ORDINAL && type.isEnum() && !converters.containsKey(type)) {
            return ordinalEnumExtractor(type);
        }
        return extractorFor(type);
    }

    public PositionalExtractor positionalExtractorFor(Class<?> type, EnumType enumType) {
        if (converters.containsKey(type)) {
            return null;
        }
        if (type.isEnum()) {
            return enumType == EnumType.ORDINAL ? ordinalEnumByIndex(type) : enumByIndex(type);
        }
        return builtInByIndex.get(type);
    }

    public Object toParameter(Object value, EnumType enumType) {
        if (enumType == EnumType.ORDINAL && value instanceof Enum<?> constant
                && !converters.containsKey(value.getClass())) {
            return constant.ordinal();
        }
        return toParameter(value);
    }

    public boolean hasConverter(Class<?> type) {
        return converters.containsKey(type);
    }

    public boolean supports(Class<?> type) {
        return resolved.computeIfAbsent(type, this::resolve) != NONE;
    }

    @SuppressWarnings("unchecked")
    public Object toParameter(Object value) {
        if (value == null) {
            return null;
        }
        ColumnTypeConverter<Object> converter =
                (ColumnTypeConverter<Object>) converters.get(value.getClass());
        if (converter != null) {
            return converter.toParameter(value);
        }
        if (value instanceof Enum<?> constant) {
            return constant.name();
        }
        return value;
    }

    private ColumnExtractor resolve(Class<?> type) {
        ColumnTypeConverter<?> converter = converters.get(type);
        if (converter != null) {
            return converter::fromColumn;
        }
        ColumnExtractor extractor = builtIn.get(type);
        if (extractor != null) {
            return extractor;
        }
        if (type.isEnum()) {
            return enumExtractor(type);
        }
        return NONE;
    }

    private ColumnExtractor ordinalEnumExtractor(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        return (resultSet, column) -> {
            int ordinal = resultSet.getInt(column);
            if (resultSet.wasNull()) {
                return null;
            }
            if (ordinal < 0 || ordinal >= constants.length) {
                throw new java.sql.SQLException(
                        "column %s holds ordinal %d, but %s has %d constants"
                                .formatted(column, ordinal, type.getSimpleName(), constants.length));
            }
            return constants[ordinal];
        };
    }

    private ColumnExtractor enumExtractor(Class<?> type) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<? extends Enum> enumType = (Class<? extends Enum>) type;
        return (resultSet, column) -> {
            String name = resultSet.getString(column);
            if (name == null) {
                return null;
            }
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object constant = Enum.valueOf(enumType, name.trim());
            return constant;
        };
    }

    private PositionalExtractor ordinalEnumByIndex(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        return (resultSet, index) -> {
            int ordinal = resultSet.getInt(index);
            if (resultSet.wasNull()) {
                return null;
            }
            if (ordinal < 0 || ordinal >= constants.length) {
                throw new SQLException(
                        "column %d holds ordinal %d, but %s has %d constants"
                                .formatted(index, ordinal, type.getSimpleName(), constants.length));
            }
            return constants[ordinal];
        };
    }

    private PositionalExtractor enumByIndex(Class<?> type) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<? extends Enum> enumType = (Class<? extends Enum>) type;
        return (resultSet, index) -> {
            String name = resultSet.getString(index);
            if (name == null) {
                return null;
            }
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object constant = Enum.valueOf(enumType, name.trim());
            return constant;
        };
    }

    private PositionalExtractor nullableByIndex(PositionalExtractor extractor) {
        return (resultSet, index) -> {
            Object value = extractor.extract(resultSet, index);
            return resultSet.wasNull() ? null : value;
        };
    }

    private ColumnExtractor nullable(ColumnExtractor extractor) {
        return (resultSet, column) -> {
            Object value = extractor.extract(resultSet, column);
            return resultSet.wasNull() ? null : value;
        };
    }

    private void registerBuiltIns() {
        builtIn.put(Integer.class, nullable(ResultSet::getInt));
        builtIn.put(int.class, ResultSet::getInt);
        builtIn.put(Long.class, nullable(ResultSet::getLong));
        builtIn.put(long.class, ResultSet::getLong);
        builtIn.put(Double.class, nullable(ResultSet::getDouble));
        builtIn.put(double.class, ResultSet::getDouble);
        builtIn.put(Float.class, nullable(ResultSet::getFloat));
        builtIn.put(float.class, ResultSet::getFloat);
        builtIn.put(Short.class, nullable(ResultSet::getShort));
        builtIn.put(short.class, ResultSet::getShort);
        builtIn.put(Byte.class, nullable(ResultSet::getByte));
        builtIn.put(byte.class, ResultSet::getByte);
        builtIn.put(Boolean.class, nullable(ResultSet::getBoolean));
        builtIn.put(boolean.class, ResultSet::getBoolean);
        builtIn.put(String.class, ResultSet::getString);
        builtIn.put(BigDecimal.class, ResultSet::getBigDecimal);
        builtIn.put(byte[].class, ResultSet::getBytes);
        builtIn.put(Object.class, ResultSet::getObject);

        builtIn.put(java.sql.Date.class, ResultSet::getDate);
        builtIn.put(java.sql.Time.class, ResultSet::getTime);
        builtIn.put(java.sql.Timestamp.class, ResultSet::getTimestamp);
        builtIn.put(java.sql.Blob.class, ResultSet::getBlob);

        objectType(LocalDate.class);
        objectType(LocalDateTime.class);
        objectType(LocalTime.class);
        objectType(OffsetDateTime.class);
        objectType(OffsetTime.class);
        objectType(Instant.class);
        objectType(UUID.class);

        builtIn.put(BigInteger.class, (resultSet, column) -> {
            BigDecimal value = resultSet.getBigDecimal(column);
            return value == null ? null : value.toBigInteger();
        });

        registerBuiltInsByIndex();
    }

    private void registerBuiltInsByIndex() {
        builtInByIndex.put(Integer.class, nullableByIndex(ResultSet::getInt));
        builtInByIndex.put(int.class, ResultSet::getInt);
        builtInByIndex.put(Long.class, nullableByIndex(ResultSet::getLong));
        builtInByIndex.put(long.class, ResultSet::getLong);
        builtInByIndex.put(Double.class, nullableByIndex(ResultSet::getDouble));
        builtInByIndex.put(double.class, ResultSet::getDouble);
        builtInByIndex.put(Float.class, nullableByIndex(ResultSet::getFloat));
        builtInByIndex.put(float.class, ResultSet::getFloat);
        builtInByIndex.put(Short.class, nullableByIndex(ResultSet::getShort));
        builtInByIndex.put(short.class, ResultSet::getShort);
        builtInByIndex.put(Byte.class, nullableByIndex(ResultSet::getByte));
        builtInByIndex.put(byte.class, ResultSet::getByte);
        builtInByIndex.put(Boolean.class, nullableByIndex(ResultSet::getBoolean));
        builtInByIndex.put(boolean.class, ResultSet::getBoolean);
        builtInByIndex.put(String.class, ResultSet::getString);
        builtInByIndex.put(BigDecimal.class, ResultSet::getBigDecimal);
        builtInByIndex.put(byte[].class, ResultSet::getBytes);
        builtInByIndex.put(Object.class, ResultSet::getObject);

        builtInByIndex.put(java.sql.Date.class, ResultSet::getDate);
        builtInByIndex.put(java.sql.Time.class, ResultSet::getTime);
        builtInByIndex.put(java.sql.Timestamp.class, ResultSet::getTimestamp);
        builtInByIndex.put(java.sql.Blob.class, ResultSet::getBlob);

        objectTypeByIndex(LocalDate.class);
        objectTypeByIndex(LocalDateTime.class);
        objectTypeByIndex(LocalTime.class);
        objectTypeByIndex(OffsetDateTime.class);
        objectTypeByIndex(OffsetTime.class);
        objectTypeByIndex(Instant.class);
        objectTypeByIndex(UUID.class);

        builtInByIndex.put(BigInteger.class, (resultSet, index) -> {
            BigDecimal value = resultSet.getBigDecimal(index);
            return value == null ? null : value.toBigInteger();
        });
    }

    private void objectType(Class<?> type) {
        builtIn.put(type, (resultSet, column) -> resultSet.getObject(column, type));
    }

    private void objectTypeByIndex(Class<?> type) {
        builtInByIndex.put(type, (resultSet, index) -> resultSet.getObject(index, type));
    }

    private static final ColumnExtractor NONE = (resultSet, column) -> null;
}
