package com.kfyty.loveqq.framework.core.support.json;

import com.kfyty.loveqq.framework.core.converter.StringToLocalDateTimeConverter;
import com.kfyty.loveqq.framework.core.lang.util.LinkedArrayList;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
public class Array extends AbstractList<Object> implements List<Object>, JSONAware {
    /**
     * 包装
     */
    private final List<Object> decorate;

    public Array() {
        this.decorate = new LinkedArrayList<>(8);
    }

    public String getString(int index) {
        Object o = this.decorate.get(index);
        return o == null ? null : o instanceof CharSequence ? o.toString() : JsonUtil.toJSONString(o);
    }

    public Boolean getBoolean(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Boolean) {
            return (Boolean) o;
        }
        return Boolean.parseBoolean(o.toString());
    }

    public Character getChar(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Character) {
            return (Character) o;
        }
        String str = o.toString();
        return str.isEmpty() ? null : str.charAt(0);
    }

    public Byte getByte(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Byte) {
            return (Byte) o;
        }
        return Byte.parseByte(o.toString());
    }

    public Short getShort(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Short) {
            return (Short) o;
        }
        return Short.parseShort(o.toString());
    }

    public Integer getInteger(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Integer) {
            return (Integer) o;
        }
        return Integer.parseInt(o.toString());
    }

    public Long getLong(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Long) {
            return (Long) o;
        }
        return Long.parseLong(o.toString());
    }

    public Float getFloat(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Float) {
            return (Float) o;
        }
        return Float.parseFloat(o.toString());
    }

    public Double getDouble(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof Double) {
            return (Double) o;
        }
        return Double.parseDouble(o.toString());
    }

    public BigInteger getBigInteger(int index) {
        Object o = this.decorate.get(index);
        return o == null || o instanceof BigInteger ? (BigInteger) o : new BigInteger(o.toString());
    }

    public BigDecimal getBigDecimal(int index) {
        Object o = this.decorate.get(index);
        return o == null || o instanceof BigDecimal ? (BigDecimal) o : new BigDecimal(o.toString());
    }

    public LocalTime getLocalTime(int index) {
        Object o = this.decorate.get(index);
        return o == null || o instanceof LocalTime ? (LocalTime) o : LocalTime.parse(o.toString());
    }

    public LocalDate getLocalDate(int index) {
        Object o = this.decorate.get(index);
        return o == null || o instanceof LocalDate ? (LocalDate) o : LocalDate.parse(o.toString());
    }

    public LocalDateTime getLocalDateTime(int index) {
        Object o = this.decorate.get(index);
        if (o == null || o instanceof LocalDateTime) {
            return (LocalDateTime) o;
        }
        return StringToLocalDateTimeConverter.INSTANCE.apply(o.toString());
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(int index, Class<T> clazz) {
        Object o = this.decorate.get(index);
        if (o == null || clazz.isInstance(o)) {
            return (T) o;
        }
        return JsonUtil.toObject(o instanceof CharSequence ? o.toString() : JsonUtil.toJSONString(o), clazz);
    }

    public <T> T getObject(int index, Type type) {
        Object o = this.decorate.get(index);
        return o == null ? null : JsonUtil.toObject(o instanceof CharSequence ? o.toString() : JsonUtil.toJSONString(o), type);
    }

    public JSON getJSON(int index) {
        return this.getJSON(this.decorate.get(index), "Not JSON for index: " + index);
    }

    public Array getArray(int index) {
        return this.getArray(this.decorate.get(index), "Not Array for index: " + index);
    }

    @Override
    public boolean isEmpty() {
        return this.decorate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.decorate.contains(o);
    }

    @Override
    public boolean add(Object instance) {
        return this.decorate.add(instance);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.decorate.containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.decorate.retainAll(c);
    }

    @Override
    public int indexOf(Object o) {
        return this.decorate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.decorate.lastIndexOf(o);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return this.decorate.subList(fromIndex, toIndex);
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
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.decorate.toArray(generator);
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
        if (o == this) {
            return true;
        }
        if (o instanceof Array) {
            return this.decorate.equals(((Array) o).decorate);
        }
        return false;
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
