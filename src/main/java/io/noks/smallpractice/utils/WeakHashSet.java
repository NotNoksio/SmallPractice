package io.noks.smallpractice.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.WeakHashMap;

public class WeakHashSet<E> extends AbstractSet<E> {
    private WeakHashMap<E, Object> map = new WeakHashMap<>();
    private static final Object PRESENT = new Object();

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
