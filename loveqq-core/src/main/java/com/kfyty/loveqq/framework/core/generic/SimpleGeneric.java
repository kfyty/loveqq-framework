package com.kfyty.loveqq.framework.core.generic;

import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * 忽略的嵌套泛型的 class
     */
    public static final List<Class<?>> IGNORED_NESTED_GENERIC_CLASSES = new LinkedList<>();

    static {
        IGNORED_NESTED_GENERIC_CLASSES.add(Lazy.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(Class.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(Supplier.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(Consumer.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(Function.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(BiConsumer.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(BiFunction.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(Comparable.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(FactoryBean.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(ApplicationEvent.class);
        IGNORED_NESTED_GENERIC_CLASSES.add(ApplicationListener.class);
    }

    /**
     * 注册忽略的嵌套泛型的 class
     *
     * @param classes class
     */
    public static void registryIgnoredClass(Class<?>... classes) {
        IGNORED_NESTED_GENERIC_CLASSES.addAll(Arrays.asList(classes));
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
     * 返回是否是简单的参数化类型，即泛型参数只有一个的泛型，或 Map 泛型
     * eg: <code>List<String><code/>
     * eg: <code>List<String[]><code/>
     * eg: <code>String[]<code/>
     *
     * @return true if simple generic
     */
    public boolean isSimpleGeneric() {
        return this.resolveType instanceof Class<?> && this.hasGeneric() || this.size() == 1 || this.isMapGeneric();
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
     * 忽略 {@link this#IGNORED_NESTED_GENERIC_CLASSES} 的泛型类型
     *
     * @return 泛型
     */
    public Class<?> getSimpleType() {
        // 自定义的类实现或继承具有泛型的接口，应该返回自定义的类型
        Class<?> rawType = getRawType(this.resolveType);
        if (this.resolveType instanceof Class<?> && this.hasGeneric() && this.getFirst() instanceof SuperGeneric) {
            return rawType;
        }
        Class<?> actualType = this.getSimpleActualType();
        if (actualType == Object.class) {
            return rawType;
        }
        for (Class<?> clazz : IGNORED_NESTED_GENERIC_CLASSES) {
            if (clazz == rawType && this.resolveType instanceof ParameterizedType) {
                return rawType;
            }
        }
        return actualType;
    }

    /**
     * 返回简单的实际泛型
     * 不忽略 {@link #IGNORED_NESTED_GENERIC_CLASSES} 的泛型类型
     *
     * @return 泛型
     */
    public Class<?> getSimpleActualType() {
        if (isMapGeneric()) {
            return this.getMapValueType().get();
        }
        if (!isSimpleGeneric()) {
            return getRawType(this.resolveType);
        }
        return getFirst().get();
    }

    @Override
    protected SimpleGeneric create(Class<?> sourceType, Type resolveType) {
        return new SimpleGeneric(sourceType, resolveType);
    }

    /*---------------------------------------------------- 静态方法 ----------------------------------------------------*/

    public static SimpleGeneric from(Class<?> clazz) {
        return (SimpleGeneric) new SimpleGeneric(clazz).resolve();
    }

    public static SimpleGeneric from(Field field) {
        return (SimpleGeneric) new SimpleGeneric(field.getDeclaringClass(), field.getGenericType()).resolve();
    }

    public static SimpleGeneric from(Method method) {
        return (SimpleGeneric) new SimpleGeneric(method.getDeclaringClass(), method.getGenericReturnType()).resolve();
    }

    public static SimpleGeneric from(Parameter parameter) {
        return (SimpleGeneric) new SimpleGeneric(parameter.getDeclaringExecutable().getDeclaringClass(), parameter.getParameterizedType()).resolve();
    }

    public static SimpleGeneric from(ParameterizedType parameterizedType) {
        return (SimpleGeneric) new SimpleGeneric(parameterizedType).resolve();
    }

    public static SimpleGeneric from(Class<?> sourceType, Field field) {
        return (SimpleGeneric) new SimpleGeneric(sourceType, field.getGenericType()).resolve();
    }

    public static SimpleGeneric from(Class<?> sourceType, Method method) {
        return (SimpleGeneric) new SimpleGeneric(sourceType, method.getGenericReturnType()).resolve();
    }

    public static SimpleGeneric from(Class<?> sourceType, Parameter parameter) {
        return (SimpleGeneric) new SimpleGeneric(sourceType, parameter.getParameterizedType()).resolve();
    }
}
