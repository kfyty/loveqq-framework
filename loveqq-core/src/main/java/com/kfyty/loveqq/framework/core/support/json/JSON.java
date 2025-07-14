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
public class JSON implements Map<String, Object>, JSONAware {
    /**
     * 包装
     */
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
        if (o == null || o instanceof Boolean) {
            return (Boolean) o;
        }
        return Boolean.parseBoolean(o.toString());
    }

    public Character getChar(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Character) {
            return (Character) o;
        }
        String str = o.toString();
        return str.isEmpty() ? null : str.charAt(0);
    }

    public Byte getByte(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Byte) {
            return (Byte) o;
        }
        return Byte.parseByte(o.toString());
    }

    public Short getShort(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Short) {
            return (Short) o;
        }
        return Short.parseShort(o.toString());
    }

    public Integer getInteger(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Integer) {
            return (Integer) o;
        }
        return Integer.parseInt(o.toString());
    }

    public Long getLong(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Long) {
            return (Long) o;
        }
        return Long.parseLong(o.toString());
    }

    public Float getFloat(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Float) {
            return (Float) o;
        }
        return Float.parseFloat(o.toString());
    }

    public Double getDouble(String key) {
        Object o = this.decorate.get(key);
        if (o == null || o instanceof Double) {
            return (Double) o;
        }
        return Double.parseDouble(o.toString());
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
        return StringToLocalDateTimeConverter.INSTANCE.apply(o.toString());
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        Object o = this.decorate.get(key);
        if (o == null || clazz.isInstance(o)) {
            return (T) o;
        }
        return JsonUtil.toObject(o.toString(), clazz);
    }

    public <T> T getObject(String key, Type type) {
        Object o = this.decorate.get(key);
        return o == null ? null : JsonUtil.toObject(o.toString(), type);
    }

    public JSON getJSON(String key) {
        return this.getJSON(this.decorate.get(key), "Not JSON for key: " + key);
    }

    public Array getArray(String key) {
        return this.getArray(this.decorate.get(key), "Not Array for key: " + key);
    }

    @Override
    public boolean isEmpty() {
        return this.decorate.isEmpty();
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
    public Object getOrDefault(Object key, Object defaultValue) {
        return this.decorate.getOrDefault(key, defaultValue);
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
        if (o == this) {
            return true;
        }
        if (o instanceof JSON json) {
            return this.decorate.equals(json.decorate);
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
