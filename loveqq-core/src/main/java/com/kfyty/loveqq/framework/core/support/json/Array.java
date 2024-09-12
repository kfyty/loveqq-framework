package com.kfyty.loveqq.framework.core.support.json;

import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * 描述: JSON Array
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class Array extends AbstractList<Object> implements JSONAware {
    private final List<Object> decorate;

    public Array() {
        this.decorate = new LinkedList<>();
    }

    public JSON getJSON(int index) {
        return getJSON(this.decorate.get(index), "Not JSON for index: " + index);
    }

    public Array getArray(int index) {
        return getArray(this.decorate.get(index), "Not Array for index: " + index);
    }

    @Override
    public Object set(int index, Object element) {
        return this.decorate.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        this.decorate.add(index, element);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        return this.decorate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        return this.decorate.addAll(index, c);
    }

    @Override
    public Object remove(int index) {
        return this.decorate.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return this.decorate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.decorate.removeAll(c);
    }

    @Override
    public Object[] toArray() {
        return this.decorate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.decorate.toArray(a);
    }

    @Override
    public void replaceAll(UnaryOperator<Object> operator) {
        this.decorate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super Object> c) {
        this.decorate.sort(c);
    }

    @Override
    public Spliterator<Object> spliterator() {
        return this.decorate.spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.decorate.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super Object> filter) {
        return this.decorate.removeIf(filter);
    }

    @Override
    public Stream<Object> stream() {
        return this.decorate.stream();
    }

    @Override
    public Stream<Object> parallelStream() {
        return this.decorate.parallelStream();
    }

    @Override
    public Object get(int index) {
        return this.decorate.get(index);
    }

    @Override
    public int size() {
        return this.decorate.size();
    }

    @Override
    public Iterator<Object> iterator() {
        return this.decorate.iterator();
    }

    @Override
    public ListIterator<Object> listIterator() {
        return this.decorate.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return this.decorate.listIterator(index);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        this.decorate.forEach(action);
    }

    @Override
    public void clear() {
        this.decorate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return this.decorate.equals(o);
    }

    @Override
    public int hashCode() {
        return this.decorate.hashCode();
    }

    @Override
    public String toString() {
        return JsonUtil.toJSONString(this.decorate);
    }
}
