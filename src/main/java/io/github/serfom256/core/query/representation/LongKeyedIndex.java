package io.github.serfom256.core.query.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

final class LongKeyedIndex implements InstanceIndex {

    private long[] keys;
    private Object[] slots;
    private boolean[] used;
    private int mask;
    private int occupied;
    private Object[] order;
    private int size;

    LongKeyedIndex() {
        allocate(16);
        order = new Object[16];
    }

    private void allocate(int capacity) {
        keys = new long[capacity];
        slots = new Object[capacity];
        used = new boolean[capacity];
        mask = capacity - 1;
    }

    private int slotFor(long key) {
        int index = (int) (key ^ (key >>> 32)) & mask;
        while (used[index] && keys[index] != key) {
            index = (index + 1) & mask;
        }
        return index;
    }

    @Override
    public Object get(long key) {
        int index = slotFor(key);
        return used[index] ? slots[index] : null;
    }

    @Override
    public Object get(Object key) {
        return key instanceof Number number ? get(number.longValue()) : null;
    }

    @Override
    public void put(long key, Object instance) {
        int index = slotFor(key);
        if (!used[index]) {
            used[index] = true;
            keys[index] = key;
            occupied++;
            append(instance);
        }
        slots[index] = instance;
        if (occupied * 2 >= keys.length) {
            grow();
        }
    }

    @Override
    public void put(Object key, Object instance) {
        if (key instanceof Number number) {
            put(number.longValue(), instance);
        }
    }

    private void append(Object instance) {
        if (size == order.length) {
            order = Arrays.copyOf(order, size * 2);
        }
        order[size++] = instance;
    }

    private void grow() {
        long[] oldKeys = keys;
        Object[] oldSlots = slots;
        boolean[] oldUsed = used;
        allocate(oldKeys.length * 2);
        for (int i = 0; i < oldUsed.length; i++) {
            if (oldUsed[i]) {
                int index = slotFor(oldKeys[i]);
                used[index] = true;
                keys[index] = oldKeys[i];
                slots[index] = oldSlots[i];
            }
        }
    }

    @Override
    public Collection<Object> values() {
        Collection<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(order[i]);
        }
        return result;
    }
}
