package com.kfyty.loveqq.framework.core.support.json;

import com.kfyty.loveqq.framework.core.converter.StringToLocalDateTimeConverter;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 描述: JSON
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class JSON extends AbstractMap<String, Object> implements JSONAware {
    private final Map<String, Object> decorate;

    public JSON() {
        this.decorate = new LinkedHashMap<>();
    }

    public String getString(String key) {
        Object o = this.decorate.get(key);
        return o == null ? null : o instanceof CharSequence ? o.toString() : JsonUtil.toJSONString(o);
    }

    public Boolean getBoolean(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Boolean ? (Boolean) o : Boolean.parseBoolean(o.toString());
    }

    public Byte getByte(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Byte ? (Byte) o : Byte.parseByte(o.toString());
    }

    public Short getShort(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Short ? (Short) o : Short.parseShort(o.toString());
    }

    public Integer getInteger(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Integer ? (Integer) o : Integer.parseInt(o.toString());
    }

    public Long getLong(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Long ? (Long) o : Long.parseLong(o.toString());
    }

    public Float getFloat(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Float ? (Float) o : Float.parseFloat(o.toString());
    }

    public Double getDouble(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof Double ? (Double) o : Double.parseDouble(o.toString());
    }

    public BigInteger getBigInteger(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof BigInteger ? (BigInteger) o : new BigInteger(o.toString());
    }

    public BigDecimal getBigDecimal(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof BigDecimal ? (BigDecimal) o : new BigDecimal(o.toString());
    }

    public LocalTime getLocalTime(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof LocalTime ? (LocalTime) o : LocalTime.parse(o.toString());
    }

    public LocalDate getLocalDate(String key) {
        Object o = this.decorate.get(key);
        return o == null || o instanceof LocalDate ? (LocalDate) o : LocalDate.parse(o.toString());
    }

    public LocalDateTime getLocalDateTime(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof LocalDateTime) {
            return (LocalDateTime) o;
        }
        return new StringToLocalDateTimeConverter().apply(o.toString());
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return JsonUtil.toObject(this.getString(key), clazz);
    }

    public <T> T getObject(String key, Type type) {
        return JsonUtil.toObject(this.getString(key), type);
    }

    public JSON getJSON(String key) {
        return getJSON(this.decorate.get(key), "Not JSON for key: " + key);
    }

    public Array getArray(String key) {
        return getArray(this.decorate.get(key), "Not Array for key: " + key);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.decorate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.decorate.containsValue(value);
    }

    @Override
    public Object remove(Object key) {
        return this.decorate.remove(key);
    }

    @Override
    public Object get(Object key) {
        return this.decorate.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return this.decorate.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        this.decorate.putAll(m);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        this.decorate.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        this.decorate.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return this.decorate.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return this.decorate.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return this.decorate.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return this.decorate.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return this.decorate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return this.decorate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return this.decorate.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return this.decorate.merge(key, value, remappingFunction);
    }

    @Override
    public int size() {
        return this.decorate.size();
    }

    @Override
    public void clear() {
        this.decorate.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.decorate.keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.decorate.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.decorate.entrySet();
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
