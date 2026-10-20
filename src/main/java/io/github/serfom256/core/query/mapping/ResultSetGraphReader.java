package io.github.serfom256.core.query.mapping;

import io.github.serfom256.core.query.representation.EntityGraph;
import io.github.serfom256.core.query.representation.InstanceIndex;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.exceptions.internal.ResultSetExtractionException;

import java.sql.ResultSet;
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
import java.util.List;
import java.util.Set;

public class ResultSetGraphReader {

    private final MappingPlanRegistry registry;

    public ResultSetGraphReader(MappingPlanRegistry registry) {
        this.registry = registry;
    }

    public Object map(Class<?> rootType, boolean iterable, ResultSet resultSet) throws SQLException {
        RowLayout layout = RowLayout.of(resultSet);
        MappingPlan rootPlan = registry.planFor(rootType);
        PreparedPlans plans = new PreparedPlans(layout);

        if (!wiresAssociations(rootPlan)) {
            return mapFlat(plans.of(rootPlan), iterable, resultSet, plans);
        }

        EntityGraph graph = new EntityGraph();
        RowReader rowReader = new RowReader(graph, plans);

        while (resultSet.next()) {
            rowReader.read(plans.of(rootPlan), resultSet);
        }

        Collection<Object> roots = graph.forType(rootType, rootPlan.hasNumericPrimaryKey()).values();
        if (iterable) {
            return new ArrayList<>(roots);
        }
        return roots.isEmpty() ? null : roots.iterator().next();
    }

    private Object mapFlat(Prepared prepared, boolean iterable, ResultSet resultSet, PreparedPlans plans)
            throws SQLException {
        List<Object> rows = iterable ? new ArrayList<>() : null;
        while (resultSet.next()) {
            if (skipsRow(prepared, resultSet)) {
                continue;
            }
            Prepared rowPlan = resolve(prepared, resultSet, plans);
            Object instance = rowPlan.plan.instantiate(resultSet, rowPlan.writer, rowPlan.indices, null);
            applyNested(rowPlan, instance, resultSet);
            if (!iterable) {
                return instance;
            }
            rows.add(instance);
        }
        return iterable ? rows : null;
    }

    private boolean skipsRow(Prepared prepared, ResultSet resultSet) throws SQLException {
        if (!prepared.keyPresent) {
            return false;
        }
        if (prepared.numericKey) {
            prepared.plan.readPrimaryKeyLong(resultSet, prepared.keyIndex);
            return resultSet.wasNull();
        }
        return prepared.plan.readPrimaryKey(resultSet, prepared.keyIndex) == null;
    }

    public Stream<Object> stream(Class<?> rootType, ResultSet resultSet) throws SQLException {
        MappingPlan rootPlan = registry.planFor(rootType);
        PreparedPlans plans = new PreparedPlans(RowLayout.of(resultSet));

        Iterator<Object> rows = wiresAssociations(rootPlan)
                ? new WindowedGraphIterator(rootPlan, resultSet, plans)
                : new FlatRowIterator(rootPlan, resultSet, plans);

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

    private Prepared resolve(Prepared prepared, ResultSet resultSet, PreparedPlans plans) throws SQLException {
        if (prepared.discriminatorIndex == RowLayout.ABSENT) {
            return prepared;
        }
        String value = resultSet.getString(prepared.discriminatorIndex);
        if (value == null) {
            return prepared;
        }
        Class<?> subtype = prepared.plan.subtypeFor(value);
        if (subtype == null) {
            throw new ModelValidationException(prepared.plan.type(),
                    "no @Subtype declares discriminator %s = '%s'"
                            .formatted(prepared.plan.discriminatorColumn(), value));
        }
        return plans.of(registry.planFor(subtype));
    }

    private void applyNested(Prepared prepared, Object instance, ResultSet resultSet) throws SQLException {
        for (int i = 0; i < prepared.nested.length; i++) {
            prepared.nested[i].apply(instance, resultSet, prepared.nestedWriters[i]);
        }
    }

    private final class PreparedPlans {

        private final RowLayout layout;
        private final IdentityHashMap<MappingPlan, Prepared> cache = new IdentityHashMap<>();
        private MappingPlan cachedPlan;
        private Prepared cachedPrepared;

        PreparedPlans(RowLayout layout) {
            this.layout = layout;
        }

        Prepared of(MappingPlan plan) {
            if (cachedPlan == plan) {
                return cachedPrepared;
            }
            Prepared prepared = cache.get(plan);
            if (prepared == null) {
                prepared = new Prepared(plan, layout, registry);
                cache.put(plan, prepared);
            }
            cachedPlan = plan;
            cachedPrepared = prepared;
            return prepared;
        }
    }

    private static final class Prepared {

        private final MappingPlan plan;
        private final boolean keyPresent;
        private final int keyIndex;
        private final boolean numericKey;
        private final RowWriter writer;
        private final int[] indices;
        private final int discriminatorIndex;
        private final NestedBinding[] nested;
        private final RowWriter[] nestedWriters;
        private final PreparedAssociation[] associations;

        Prepared(MappingPlan plan, RowLayout layout, MappingPlanRegistry registry) {
            this.plan = plan;
            this.keyPresent = layout.has(plan.primaryKeyColumn());
            this.keyIndex = layout.indexOf(plan.primaryKeyColumn());
            this.numericKey = plan.hasNumericPrimaryKey();
            this.writer = plan.writerFor(layout);
            this.indices = plan.indicesFor(layout);
            this.discriminatorIndex = layout.indexOf(plan.discriminatorColumn());

            List<NestedBinding> nestedBindings = plan.nested();
            this.nested = nestedBindings.toArray(new NestedBinding[0]);
            this.nestedWriters = new RowWriter[nested.length];
            for (int i = 0; i < nested.length; i++) {
                nestedWriters[i] = nested[i].writerFor(layout);
            }

            List<AssociationBinding> bindings = plan.associations();
            this.associations = new PreparedAssociation[bindings.size()];
            for (int i = 0; i < associations.length; i++) {
                AssociationBinding association = bindings.get(i);
                MappingPlan targetPlan = registry.planFor(association.targetType());
                associations[i] = new PreparedAssociation(association, targetPlan,
                        layout.has(targetPlan.primaryKeyColumn()),
                        layout.indexOf(plan.columnForField(association.joinByField())),
                        layout.indexOf(targetPlan.columnForField(association.toField())));
            }
        }
    }

    private static final class PreparedAssociation {

        private final AssociationBinding association;
        private final MappingPlan targetPlan;
        private final boolean targetKeyPresent;
        private final int ownerJoinIndex;
        private final int targetJoinIndex;

        PreparedAssociation(AssociationBinding association, MappingPlan targetPlan, boolean targetKeyPresent,
                            int ownerJoinIndex, int targetJoinIndex) {
            this.association = association;
            this.targetPlan = targetPlan;
            this.targetKeyPresent = targetKeyPresent;
            this.ownerJoinIndex = ownerJoinIndex;
            this.targetJoinIndex = targetJoinIndex;
        }
    }

    private final class FlatRowIterator implements Iterator<Object> {

        private final Prepared prepared;
        private final ResultSet resultSet;
        private final PreparedPlans plans;
        private Boolean hasNext;

        FlatRowIterator(MappingPlan plan, ResultSet resultSet, PreparedPlans plans) {
            this.prepared = plans.of(plan);
            this.resultSet = resultSet;
            this.plans = plans;
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
                Prepared rowPlan = resolve(prepared, resultSet, plans);
                Object instance = rowPlan.plan.instantiate(resultSet, rowPlan.writer, rowPlan.indices, null);
                applyNested(rowPlan, instance, resultSet);
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

        private final Prepared prepared;
        private final ResultSet resultSet;
        private final PreparedPlans plans;
        private final Set<Object> emittedKeys = new HashSet<>();

        private RowReader windowReader;
        private Object pending;
        private Object pendingKey;
        private boolean rowHeld;
        private boolean exhausted;
        private Object next;

        WindowedGraphIterator(MappingPlan plan, ResultSet resultSet, PreparedPlans plans) {
            this.prepared = plans.of(plan);
            this.resultSet = resultSet;
            this.plans = plans;
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
                    Object key = prepared.plan.readPrimaryKey(resultSet, prepared.keyIndex);
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
                    pending = windowReader.read(prepared, resultSet);
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
                                .formatted(prepared.plan.type().getSimpleName(), key)));
            }
            pendingKey = key;
            windowReader = new RowReader(new EntityGraph(), plans);
        }

        private Object emit() {
            Object completed = pending;
            pending = null;
            pendingKey = null;
            return completed;
        }
    }

    private final class RowReader {

        private final EntityGraph graph;
        private final PreparedPlans plans;
        private Class<?> cachedType;
        private InstanceIndex cachedIndex;
        private Set<Object> inProgress;
        private IdentityHashMap<Object, Set<Object>> collectionMembers;

        RowReader(EntityGraph graph, PreparedPlans plans) {
            this.graph = graph;
            this.plans = plans;
        }

        private InstanceIndex indexFor(Prepared prepared) {
            if (cachedType != prepared.plan.type()) {
                cachedType = prepared.plan.type();
                cachedIndex = graph.forType(cachedType, prepared.numericKey);
            }
            return cachedIndex;
        }

        Object read(Prepared prepared, ResultSet resultSet) throws SQLException {
            if (!prepared.keyPresent) {
                return null;
            }

            InstanceIndex instances = indexFor(prepared);
            long longKey = 0;
            Object primaryKey = null;
            Object instance;
            if (prepared.numericKey) {
                longKey = prepared.plan.readPrimaryKeyLong(resultSet, prepared.keyIndex);
                if (resultSet.wasNull()) {
                    return null;
                }
                instance = instances.get(longKey);
            } else {
                primaryKey = prepared.plan.readPrimaryKey(resultSet, prepared.keyIndex);
                if (primaryKey == null) {
                    return null;
                }
                instance = instances.get(primaryKey);
            }

            Prepared rowPlan = resolve(prepared, resultSet, plans);
            if (instance == null) {
                instance = rowPlan.plan.instantiate(resultSet, rowPlan.writer, rowPlan.indices, primaryKey);
                if (prepared.numericKey) {
                    instances.put(longKey, instance);
                } else {
                    instances.put(primaryKey, instance);
                }
                applyNested(rowPlan, instance, resultSet);
            }

            if (rowPlan.associations.length == 0) {
                return instance;
            }
            if (!inProgress().add(instance)) {
                return instance;
            }
            try {
                wireAssociations(rowPlan, instance, resultSet);
            } finally {
                inProgress.remove(instance);
            }
            return instance;
        }

        private Set<Object> inProgress() {
            if (inProgress == null) {
                inProgress = Collections.newSetFromMap(new IdentityHashMap<>());
            }
            return inProgress;
        }

        private void wireAssociations(Prepared prepared, Object instance, ResultSet resultSet) throws SQLException {
            for (PreparedAssociation prepAssociation : prepared.associations) {
                if (!prepAssociation.targetKeyPresent) {
                    continue;
                }
                AssociationBinding association = prepAssociation.association;
                if (association.isCollection()) {
                    Collection<Object> collection = association.collectionOf(instance);
                    if (!joinKeysMatch(prepAssociation, resultSet)) {
                        continue;
                    }
                    Object child = read(plans.of(prepAssociation.targetPlan), resultSet);
                    if (child != null) {
                        addUnique(collection, child);
                    }
                } else {
                    if (!joinKeysMatch(prepAssociation, resultSet)) {
                        continue;
                    }
                    Object child = read(plans.of(prepAssociation.targetPlan), resultSet);
                    if (child != null) {
                        association.setSingle(instance, child);
                    }
                }
            }
        }

        private boolean joinKeysMatch(PreparedAssociation association, ResultSet resultSet) throws SQLException {
            if (association.ownerJoinIndex == RowLayout.ABSENT
                    || association.targetJoinIndex == RowLayout.ABSENT) {
                return true;
            }
            return sameKey(resultSet.getObject(association.ownerJoinIndex),
                    resultSet.getObject(association.targetJoinIndex));
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
            if (collectionMembers == null) {
                collectionMembers = new IdentityHashMap<>();
            }
            Set<Object> members = collectionMembers.computeIfAbsent(
                    collection, c -> Collections.newSetFromMap(new IdentityHashMap<>()));
            if (members.add(child)) {
                collection.add(child);
            }
        }
    }
}
