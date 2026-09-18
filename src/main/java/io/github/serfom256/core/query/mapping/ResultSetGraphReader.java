package io.github.serfom256.core.query.mapping;

import io.github.serfom256.core.query.representation.EntityGraph;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.exceptions.internal.ResultSetExtractionException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Materialises an object graph from a result set in a single pass.
 *
 * <p>Each row already pairs a parent with one of its joined children (the SQL
 * join enforced the relationship), so relationships are wired directly from row
 * co-occurrence. An identity map deduplicates entities by primary key across the
 * repeated rows of a join, and a per-row "in progress" set guards against
 * infinite recursion on bidirectional or cyclic associations.
 *
 * <p>Complexity is O(rows x graph-depth); there is no O(parents x children)
 * matching and no reflection on the hot path.
 */
public class ResultSetGraphReader {

    private final MappingPlanRegistry registry;

    public ResultSetGraphReader(MappingPlanRegistry registry) {
        this.registry = registry;
    }

    /**
     * @param rootType the entity type to return
     * @param iterable whether the caller expects a collection or a single entity
     * @return a {@code List} of root entities when {@code iterable}, otherwise the
     *         first root entity or {@code null} when the result set is empty
     */
    public Object map(Class<?> rootType, boolean iterable, ResultSet resultSet) throws SQLException {
        Set<String> columns = columnLabels(resultSet);
        MappingPlan rootPlan = registry.planFor(rootType);
        EntityGraph graph = new EntityGraph();
        RowReader rowReader = new RowReader(graph, columns);

        while (resultSet.next()) {
            rowReader.read(rootPlan, resultSet);
        }

        Collection<Object> roots = graph.forType(rootType).values();
        if (iterable) {
            return new ArrayList<>(roots);
        }
        return roots.isEmpty() ? null : roots.iterator().next();
    }

    public Stream<Object> stream(Class<?> rootType, ResultSet resultSet) throws SQLException {
        MappingPlan rootPlan = registry.planFor(rootType);
        Set<String> columns = columnLabels(resultSet);

        Iterator<Object> rows = wiresAssociations(rootPlan)
                ? new WindowedGraphIterator(rootPlan, resultSet, columns)
                : new FlatRowIterator(rootPlan, resultSet, columns);

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(rows, Spliterator.ORDERED | Spliterator.NONNULL),
                false);
    }

    private boolean wiresAssociations(MappingPlan plan) {
        if (!plan.associations().isEmpty()) {
            return true;
        }
        for (Class<?> subtype : plan.subtypes()) {
            if (!registry.planFor(subtype).associations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private MappingPlan resolve(MappingPlan declared, ResultSet resultSet, Set<String> columns) throws SQLException {
        String column = declared.discriminatorColumn();
        if (column == null || !columns.contains(column)) {
            return declared;
        }
        String value = resultSet.getString(column);
        if (value == null) {
            return declared;
        }
        Class<?> subtype = declared.subtypeFor(value);
        if (subtype == null) {
            throw new ModelValidationException(declared.type(),
                    "no @Subtype declares discriminator %s = '%s'".formatted(column, value));
        }
        return registry.planFor(subtype);
    }

    private final class FlatRowIterator implements Iterator<Object> {

        private final MappingPlan plan;
        private final ResultSet resultSet;
        private final Set<String> columns;
        private Boolean hasNext;

        FlatRowIterator(MappingPlan plan, ResultSet resultSet, Set<String> columns) {
            this.plan = plan;
            this.resultSet = resultSet;
            this.columns = columns;
        }

        @Override
        public boolean hasNext() {
            if (hasNext == null) {
                hasNext = advance();
            }
            return hasNext;
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNext = null;
            try {
                MappingPlan rowPlan = resolve(plan, resultSet, columns);
                Object instance = rowPlan.instantiate(resultSet, columns);
                for (NestedBinding nested : rowPlan.nested()) {
                    nested.apply(instance, resultSet, columns);
                }
                return instance;
            } catch (SQLException e) {
                throw new ResultSetExtractionException(e);
            }
        }

        private boolean advance() {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                throw new ResultSetExtractionException(e);
            }
        }
    }

    private final class WindowedGraphIterator implements Iterator<Object> {

        private final MappingPlan plan;
        private final ResultSet resultSet;
        private final Set<String> columns;
        private final Set<Object> emittedKeys = new HashSet<>();

        private RowReader windowReader;
        private Object pending;
        private Object pendingKey;
        private boolean rowHeld;
        private boolean exhausted;
        private Object next;

        WindowedGraphIterator(MappingPlan plan, ResultSet resultSet, Set<String> columns) {
            this.plan = plan;
            this.resultSet = resultSet;
            this.columns = columns;
        }

        @Override
        public boolean hasNext() {
            if (next == null && !exhausted) {
                next = advance();
            }
            return next != null;
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Object current = next;
            next = null;
            return current;
        }

        private Object advance() {
            try {
                while (rowHeld || resultSet.next()) {
                    rowHeld = false;
                    Object key = plan.readPrimaryKey(resultSet);
                    if (key == null) {
                        continue;
                    }
                    if (pending != null && !key.equals(pendingKey)) {
                        rowHeld = true;
                        return emit();
                    }
                    if (pending == null) {
                        openWindow(key);
                    }
                    pending = windowReader.read(plan, resultSet);
                }
                exhausted = true;
                return pending != null ? emit() : null;
            } catch (SQLException e) {
                throw new ResultSetExtractionException(e);
            }
        }

        private void openWindow(Object key) {
            if (!emittedKeys.add(key)) {
                throw new ResultSetExtractionException(new SQLException(
                        ("rows for %s id %s are not contiguous: streaming an entity with a @Joined "
                                + "association requires the query to order by the root id, so each "
                                + "entity is complete before the next begins")
                                .formatted(plan.type().getSimpleName(), key)));
            }
            pendingKey = key;
            windowReader = new RowReader(new EntityGraph(), columns);
        }

        private Object emit() {
            Object completed = pending;
            pending = null;
            pendingKey = null;
            return completed;
        }
    }

    private static Set<String> columnLabels(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        Set<String> labels = new HashSet<>(Math.max(16, count * 2));
        for (int i = 1; i <= count; i++) {
            labels.add(meta.getColumnLabel(i));
        }
        return labels;
    }

    private final class RowReader {

        private final EntityGraph graph;
        private final Set<String> columns;
        private final Set<Object> inProgress = Collections.newSetFromMap(new IdentityHashMap<>());
        private final IdentityHashMap<Object, Set<Object>> collectionMembers = new IdentityHashMap<>();

        RowReader(EntityGraph graph, Set<String> columns) {
            this.graph = graph;
            this.columns = columns;
        }

        Object read(MappingPlan declared, ResultSet resultSet) throws SQLException {
            if (!columns.contains(declared.primaryKeyColumn())) {
                return null; // this entity type is not part of the query
            }
            Object primaryKey = declared.readPrimaryKey(resultSet);
            if (primaryKey == null) {
                return null; // outer join produced no row for this type
            }

            MappingPlan plan = resolve(declared, resultSet, columns);
            Map<Object, Object> instances = graph.forType(declared.type());
            Object instance = instances.get(primaryKey);
            if (instance == null) {
                instance = plan.instantiate(resultSet, columns);
                instances.put(primaryKey, instance);
                for (NestedBinding nested : plan.nested()) {
                    nested.apply(instance, resultSet, columns);
                }
            }

            if (!inProgress.add(instance)) {
                return instance; // already being materialised up the call stack (cycle)
            }
            try {
                wireAssociations(plan, instance, resultSet);
            } finally {
                inProgress.remove(instance);
            }
            return instance;
        }

        private void wireAssociations(MappingPlan plan, Object instance, ResultSet resultSet) throws SQLException {
            for (AssociationBinding association : plan.associations()) {
                MappingPlan childPlan = registry.planFor(association.targetType());
                if (!columns.contains(childPlan.primaryKeyColumn())) {
                    continue; // association not selected by this query
                }
                if (association.isCollection()) {
                    Collection<Object> collection = association.collectionOf(instance);
                    if (!joinKeysMatch(plan, childPlan, association, resultSet)) {
                        continue;
                    }
                    Object child = read(childPlan, resultSet);
                    if (child != null) {
                        addUnique(collection, child);
                    }
                } else {
                    if (!joinKeysMatch(plan, childPlan, association, resultSet)) {
                        continue;
                    }
                    Object child = read(childPlan, resultSet);
                    if (child != null) {
                        association.setSingle(instance, child);
                    }
                }
            }
        }

        private boolean joinKeysMatch(MappingPlan ownerPlan,
                                      MappingPlan targetPlan,
                                      AssociationBinding association,
                                      ResultSet resultSet) throws SQLException {
            String ownerColumn = ownerPlan.columnForField(association.joinByField());
            String targetColumn = targetPlan.columnForField(association.toField());
            if (ownerColumn == null || targetColumn == null
                    || !columns.contains(ownerColumn) || !columns.contains(targetColumn)) {
                return true;
            }
            return sameKey(resultSet.getObject(ownerColumn), resultSet.getObject(targetColumn));
        }

        private boolean sameKey(Object owner, Object target) {
            if (owner == null || target == null) {
                return false;
            }
            if (owner.equals(target)) {
                return true;
            }
            if (owner instanceof Number && target instanceof Number) {
                return ((Number) owner).longValue() == ((Number) target).longValue();
            }
            return false;
        }

        private void addUnique(Collection<Object> collection, Object child) {
            Set<Object> members = collectionMembers.computeIfAbsent(
                    collection, c -> Collections.newSetFromMap(new IdentityHashMap<>()));
            if (members.add(child)) {
                collection.add(child);
            }
        }
    }
}
