package com.kfyty.support.generic;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.utils.CommonUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.kfyty.support.utils.ReflectUtil.getRawType;
import static com.kfyty.support.utils.ReflectUtil.getTypeVariableName;

/**
 * 描述: 泛型描述
 *
 * @author kfyty725
 * @date 2021/6/24 13:00
 * @email kfyty725@hotmail.com
 */
@Getter
public class QualifierGeneric {
    /**
     * 源类型
     * 如果是 Class，则为 Class 本身
     * 如果是 Field，则为其属性类型
     * 如果是 Method，则为其返回值类型
     * 如果是 Parameter，则为其参数类型
     */
    protected final Class<?> sourceType;

    /**
     * 解析的目标类型
     */
    protected final Type resolveType;

    /**
     * 泛型类型，对于其 key，取值如下：
     *      如果是 Class：
     *              如果 Class 是数组类型，则为数组组件类型
     *              如果 Class 非泛型 Class，则为其父类直接泛型类型 + 接口直接泛型类型
     *              如果都不是，则为空
     *      如果是 Field，则为其类型的直接泛型
     *      如果是 Method，则为其返回值类型的直接泛型
     *      如果是 Parameter，则为其参数类型的直接泛型
     * 对于其 value，如果 key 是泛型，则递归查询，直到查询到泛型为 TypeVariable
     */
    protected final Map<Generic, QualifierGeneric> genericInfo;

    protected QualifierGeneric(Class<?> sourceType) {
        this.sourceType = sourceType;
        this.resolveType = sourceType;
        this.genericInfo = new LinkedHashMap<>(4);
    }

    protected QualifierGeneric(Class<?> sourceType, Type resolveType) {
        this.sourceType = sourceType;
        this.resolveType = resolveType;
        this.genericInfo = new LinkedHashMap<>(4);
    }

    protected QualifierGeneric create(Class<?> sourceType) {
        return new QualifierGeneric(sourceType);
    }

    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new QualifierGeneric(sourceType, resolveType);
    }

    public QualifierGeneric doResolve() {
        return this.processGenericType(this.resolveType);
    }

    public int size() {
        return this.genericInfo.size();
    }

    public Generic getFirst() {
        return this.genericInfo.keySet().iterator().next();
    }

    public Generic getSecond() {
        Iterator<Generic> iterator = this.genericInfo.keySet().iterator();
        iterator.next();
        boolean hasNext = iterator.hasNext();
        return hasNext ? iterator.next() : null;
    }

    public static QualifierGeneric from(Class<?> clazz) {
        return new QualifierGeneric(clazz).doResolve();
    }

    public static QualifierGeneric from(Field field) {
        return new QualifierGeneric(field.getType(), field.getGenericType()).doResolve();
    }

    public static QualifierGeneric from(Method method) {
        return new QualifierGeneric(method.getReturnType(), method.getGenericReturnType()).doResolve();
    }

    public static QualifierGeneric from(Parameter parameter) {
        return new QualifierGeneric(parameter.getType(), parameter.getParameterizedType()).doResolve();
    }

    private QualifierGeneric processGenericType(Type genericType) {
        if(genericType == null) {
            return this;
        }
        if(genericType instanceof Class) {
            this.processClassGenericType((Class<?>) genericType);
            return this;
        }
        if(genericType instanceof GenericArrayType) {
            this.processGenericArrayType((GenericArrayType) genericType);
            return this;
        }
        if(genericType instanceof ParameterizedType) {
            this.processParameterizedType((ParameterizedType) genericType);
            return this;
        }
        if(genericType instanceof WildcardType) {
            this.processWildcardType((WildcardType) genericType);
            return this;
        }
        if(genericType instanceof TypeVariable) {
            this.processTypeVariable((TypeVariable<?>) genericType);
            return this;
        }
        throw new SupportException("unsupported generic type !");
    }

    private void processClassGenericType(Class<?> clazz) {
        if(clazz.isArray()) {
            this.genericInfo.put(new Generic(clazz.getComponentType(), true), null);
            return;
        }
        List<Type> superGenericType = new ArrayList<>(4);
        superGenericType.add(clazz.getGenericSuperclass());
        superGenericType.addAll(Arrays.asList(clazz.getGenericInterfaces()));
        for (Type type : superGenericType) {
            if(type instanceof ParameterizedType) {
                this.processParameterizedType((ParameterizedType) type, getRawType(type));
            }
        }
    }

    private void processGenericArrayType(GenericArrayType type) {
        this.processGenericType(type.getGenericComponentType());
    }

    private void processParameterizedType(ParameterizedType type) {
        this.processParameterizedType(type, null);
    }

    private void processParameterizedType(ParameterizedType type, Class<?> superType) {
        Type[] actualTypeArguments = type.getActualTypeArguments();
        for (Type actualTypeArgument : actualTypeArguments) {
            if(actualTypeArgument instanceof Class && ((Class<?>) actualTypeArgument).isArray()) {
                this.processClassGenericType((Class<?>) actualTypeArgument);
                continue;
            }
            if(actualTypeArgument instanceof TypeVariable) {
                this.processTypeVariable((TypeVariable<?>) actualTypeArgument);
                continue;
            }
            if(actualTypeArgument instanceof GenericArrayType) {
                Type componentType = ((GenericArrayType) actualTypeArgument).getGenericComponentType();
                while (componentType instanceof GenericArrayType) {
                    componentType = ((GenericArrayType) componentType).getGenericComponentType();
                }
                if(componentType instanceof TypeVariable) {
                    this.processTypeVariable(actualTypeArgument.getTypeName(), true, superType);
                    continue;
                }
            }
            Class<?> rawType = getRawType(actualTypeArgument);
            boolean isArray = actualTypeArgument instanceof GenericArrayType;
            QualifierGeneric nested = actualTypeArgument instanceof Class ? null : create(rawType, actualTypeArgument).doResolve();
            this.genericInfo.put(superType == null ? new Generic(rawType, isArray) : new SuperGeneric(rawType, isArray, superType), nested);
        }
    }

    private void processWildcardType(WildcardType wildcardType) {
        Type type = CommonUtil.empty(wildcardType.getLowerBounds()) ? wildcardType.getUpperBounds()[0] : wildcardType.getLowerBounds()[0];
        this.processGenericType(type);
    }

    private void processTypeVariable(TypeVariable<?> typeVariable) {
        this.processTypeVariable(getTypeVariableName(typeVariable), false, null);
    }

    private void processTypeVariable(String typeVariableName, boolean isArray, Class<?> superType) {
        if(superType == null) {
            this.genericInfo.put(new Generic(typeVariableName, isArray), null);
            return;
        }
        this.genericInfo.put(new SuperGeneric(typeVariableName, isArray, superType), null);
    }
}
