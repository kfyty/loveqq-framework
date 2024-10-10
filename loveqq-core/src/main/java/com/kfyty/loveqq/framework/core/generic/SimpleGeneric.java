package com.kfyty.loveqq.framework.core.generic;

import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.reflect.ParameterizedTypeImpl;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 描述: 简单的泛型，支持简单的泛型需求
 *
 * @author kfyty725
 * @date 2021/6/24 17:34
 * @email kfyty725@hotmail.com
 */
@Getter
@Setter
public class SimpleGeneric extends QualifierGeneric {
    /**
     * 具有简单嵌套泛型的 class
     */
    public static final List<Class<?>> SIMPLE_NESTED_GENERIC_CLASSES = new LinkedList<>();

    static {
        SIMPLE_NESTED_GENERIC_CLASSES.add(Collection.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(List.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Set.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(SortedSet.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Queue.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Deque.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Map.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(SortedMap.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(TreeMap.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(ConcurrentMap.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(ConcurrentHashMap.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Value.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(LaziedObject.class);
        SIMPLE_NESTED_GENERIC_CLASSES.add(Lazy.class);
    }

    /**
     * 注册具有简单嵌套泛型的 class
     *
     * @param classes class
     */
    public static void registryNestedClass(Class<?>... classes) {
        SIMPLE_NESTED_GENERIC_CLASSES.addAll(Arrays.asList(classes));
    }

    /**
     * map key
     */
    private String mapKey;

    public SimpleGeneric(Class<?> sourceType) {
        super(sourceType);
    }

    public SimpleGeneric(Type generic) {
        super(generic);
    }

    public SimpleGeneric(Class<?> sourceType, Type generic) {
        super(sourceType, generic);
    }

    /**
     * 是否是简单的数组泛型，即只有一个泛型，且泛型是数组
     * eg: <code>String[]</code>
     *
     * @return true if simple array generic
     */
    public boolean isSimpleArray() {
        if (this.size() != 1) {
            return false;
        }
        return this.getGeneric().isArray();
    }

    /**
     * 返回是否是简单的参数化类型，即泛型参数只有一个的泛型，不包含类型变量的泛型(List<E>)
     * eg: <code>List<String><code/>
     * eg: <code>List<String[]><code/>
     * eg: <code>String[]<code/>
     *
     * @return true if simple generic
     */
    public boolean isSimpleGeneric() {
        if (this.sourceType == null) {
            return this.size() == 1;
        }
        return !Objects.equals(this.sourceType, this.resolveType) || this.isSimpleArray();
    }

    /**
     * 返回是否是 Map<K, V> 泛型
     *
     * @return true if map generic
     */
    public boolean isMapGeneric() {
        return this.isGeneric(Map.class);
    }

    /**
     * 返回 map key 泛型
     *
     * @return map key generic
     */
    public Generic getMapKeyType() {
        return this.getFirst();
    }

    /**
     * 返回 map value 泛型
     *
     * @return map value generic
     */
    public Generic getMapValueType() {
        return this.getSecond();
    }

    /**
     * 返回简单的实际泛型
     *
     * @return 泛型
     */
    public Class<?> getSimpleActualType() {
        if (isMapGeneric()) {
            return getMapValueType().get();
        }
        if (!isSimpleGeneric()) {
            return this.sourceType != null ? this.sourceType : (Class<?>) this.resolveType;
        }
        if (!isGeneric(Class.class) && !isSimpleArray() && SIMPLE_NESTED_GENERIC_CLASSES.stream().noneMatch(this::isGeneric)) {
            return this.sourceType != null ? this.sourceType : (Class<?>) this.resolveType;
        }
        return getFirst().get();
    }

    /**
     * 解析简单的嵌套的泛型
     *
     * @return 嵌套泛型
     */
    public SimpleGeneric resolveNestedGeneric() {
        for (Class<?> collectionClass : SIMPLE_NESTED_GENERIC_CLASSES) {
            QualifierGeneric nested = this.getNested(new Generic(collectionClass));
            if (nested != null) {
                return (SimpleGeneric) nested;
            }
        }
        return null;
    }

    @Override
    protected SimpleGeneric create(Class<?> sourceType, Type resolveType) {
        return new SimpleGeneric(sourceType, resolveType);
    }

    /*---------------------------------------------------- 静态方法 ----------------------------------------------------*/

    public static SimpleGeneric from(Class<?> clazz) {
        return (SimpleGeneric) new SimpleGeneric(clazz).resolve();
    }

    public static SimpleGeneric from(ParameterizedTypeImpl parameterizedType) {
        return (SimpleGeneric) new SimpleGeneric(parameterizedType).resolve();
    }

    public static SimpleGeneric from(Field field) {
        return (SimpleGeneric) new SimpleGeneric(field.getType(), field.getGenericType()).resolve();
    }

    public static SimpleGeneric from(Method method) {
        return (SimpleGeneric) new SimpleGeneric(method.getReturnType(), method.getGenericReturnType()).resolve();
    }

    public static SimpleGeneric from(Parameter parameter) {
        return (SimpleGeneric) new SimpleGeneric(parameter.getType(), parameter.getParameterizedType()).resolve();
    }
}
