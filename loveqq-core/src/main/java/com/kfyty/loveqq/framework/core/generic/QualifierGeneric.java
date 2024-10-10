package com.kfyty.loveqq.framework.core.generic;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.reflect.ParameterizedTypeImpl;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getRawType;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getTypeVariableName;
import static java.util.Optional.ofNullable;

/**
 * 描述: 全限定泛型描述
 *
 * @author kfyty725
 * @date 2021/6/24 13:00
 * @email kfyty725@hotmail.com
 */
@Getter
@EqualsAndHashCode
public class QualifierGeneric {
    /**
     * 源类型
     * 如果是 Class，则为 Class 本身
     * 如果是 Field，则为其属性类型
     * 如果是 Method，则为其返回值类型
     * 如果是 Parameter，则为其参数类型
     */
    protected Class<?> sourceType;

    /**
     * 解析的目标类型
     */
    protected Type resolveType;

    /**
     * 泛型类型，对于其 key，取值如下：
     * 如果是 Class：
     * 如果 Class 是数组类型，则为数组组件类型
     * 如果 Class 非泛型 Class，则为其父类直接泛型类型 + 接口直接泛型类型
     * 如果都不是，则为空
     * 如果是 Field，则为其类型的直接泛型
     * 如果是 Method，则为其返回值类型的直接泛型
     * 如果是 Parameter，则为其参数类型的直接泛型
     * 对于其 value，如果 key 是泛型，则递归查询，直到查询到泛型为 TypeVariable
     */
    protected final Map<Generic, QualifierGeneric> genericInfo;

    /**
     * 构建一个没有泛型的 QualifierGeneric
     *
     * @param sourceType 类型
     */
    public QualifierGeneric(Class<?> sourceType) {
        this(sourceType, null);
    }

    /**
     * 构建一个没有原始类型的泛型的 QualifierGeneric
     *
     * @param genericType 类型
     */
    public QualifierGeneric(Type genericType) {
        this(genericType instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) genericType).getRawType() : null, genericType);
    }

    /**
     * 构建一个 QualifierGeneric
     *
     * @param sourceType  原始类型
     * @param genericType 原始类型存在的泛型类型
     */
    public QualifierGeneric(Class<?> sourceType, Type genericType) {
        this.sourceType = sourceType;
        this.resolveType = genericType;
        this.genericInfo = new LinkedHashMap<>(4);
    }

    /**
     * 解析泛型
     *
     * @return this
     */
    public QualifierGeneric resolve() {
        if (!this.genericInfo.isEmpty()) {
            return this;
        }
        if (this.sourceType == this.resolveType && this.sourceType != null && !this.sourceType.isArray() && !Map.class.isAssignableFrom(this.sourceType)) {
            return this;
        }
        return this.resolveGenericType(ofNullable(this.resolveType).orElse(this.sourceType));
    }

    /**
     * 返回泛型的数量
     *
     * @return size
     */
    public int size() {
        return this.genericInfo.size();
    }

    /**
     * 返回是否存在泛型
     *
     * @return true is exists
     */
    public boolean hasGeneric() {
        return this.size() > 0;
    }

    /**
     * 是否是给定类型的泛型
     *
     * @param generic 泛型 eg: Map.class
     * @return true if is generic
     */
    public boolean isGeneric(Class<?> generic) {
        if (this.sourceType != null) {
            return generic.isAssignableFrom(this.sourceType);
        }
        return generic.isAssignableFrom(ReflectUtil.getRawType(this.resolveType));
    }

    /**
     * 获取泛型信息，如果泛型有多个则抛出异常
     *
     * @return 泛型
     */
    public Generic getGeneric() {
        if (this.size() > 1) {
            throw new ResolvableException("more than one generic !");
        }
        return this.getFirst();
    }

    /**
     * 获取嵌套的泛型
     *
     * @param generic 嵌套类型的父泛型
     * @return 嵌套的泛型
     */
    public QualifierGeneric getNested(Generic generic) {
        return this.genericInfo.get(generic);
    }

    /**
     * 获取第一个泛型
     *
     * @return 第一个泛型
     */
    public Generic getFirst() {
        if (!this.hasGeneric()) {
            throw new ResolvableException("The generic doesn't exists !");
        }
        return this.genericInfo.keySet().iterator().next();
    }

    /**
     * 获取第二个泛型
     *
     * @return 第二个泛型
     */
    public Generic getSecond() {
        if (this.size() < 2) {
            throw new ResolvableException("The generic doesn't exists !");
        }
        Iterator<Generic> iterator = this.genericInfo.keySet().iterator();
        iterator.next();
        return iterator.next();
    }

    /**
     * 构造子泛型，由子类实现，保证递推解析时，泛型实现都是同一类对象
     */
    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new QualifierGeneric(sourceType, resolveType);
    }

    /*---------------------------------------------------- 泛型解析 ----------------------------------------------------*/

    protected QualifierGeneric resolveGenericType(Type genericType) {
        if (genericType == null) {
            return this;
        }
        if (genericType instanceof Class) {
            this.resolveClassGenericType((Class<?>) genericType);
            return this;
        }
        if (genericType instanceof GenericArrayType) {
            this.resolveGenericArrayType((GenericArrayType) genericType);
            return this;
        }
        if (genericType instanceof WildcardType) {
            this.resolveWildcardType((WildcardType) genericType);
            return this;
        }
        if (genericType instanceof TypeVariable) {
            this.resolveTypeVariable((TypeVariable<?>) genericType);
            return this;
        }
        if (genericType instanceof ParameterizedType) {
            this.resolveParameterizedType((ParameterizedType) genericType);
            return this;
        }
        throw new ResolvableException("Unsupported generic type: " + genericType);
    }

    protected void resolveClassGenericType(Class<?> clazz) {
        if (clazz.isArray()) {
            this.genericInfo.put(new Generic(clazz.getComponentType(), true), null);
            return;
        }
        for (Type type : ReflectUtil.getGenerics(clazz)) {
            if (type instanceof ParameterizedType) {
                this.resolveParameterizedType((ParameterizedType) type, getRawType(type));
            }
        }
    }

    protected void resolveGenericArrayType(GenericArrayType type) {
        this.resolveGenericType(type.getGenericComponentType());
    }

    protected void resolveWildcardType(WildcardType wildcardType) {
        Type type = CommonUtil.empty(wildcardType.getLowerBounds()) ? wildcardType.getUpperBounds()[0] : wildcardType.getLowerBounds()[0];
        this.resolveGenericType(type);
    }

    protected void resolveTypeVariable(TypeVariable<?> typeVariable) {
        this.resolveTypeVariable(getTypeVariableName(typeVariable), false, null);
    }

    protected void resolveTypeVariable(String typeVariableName, boolean isArray, Class<?> superType) {
        if (superType == null) {
            this.genericInfo.put(new Generic(typeVariableName, isArray), null);
            return;
        }
        this.genericInfo.put(new SuperGeneric(typeVariableName, isArray, superType), null);
    }

    protected void resolveParameterizedType(ParameterizedType type) {
        this.resolveParameterizedType(type, null);
    }

    protected void resolveParameterizedType(ParameterizedType type, Class<?> superType) {
        Type[] actualTypeArguments = type.getActualTypeArguments();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
                this.resolveTypeVariable((TypeVariable<?>) actualTypeArgument);
                continue;
            }
            if (actualTypeArgument instanceof Class && ((Class<?>) actualTypeArgument).isArray()) {
                this.resolveClassGenericType((Class<?>) actualTypeArgument);
                continue;
            }
            if (actualTypeArgument instanceof GenericArrayType) {
                Type componentType = ((GenericArrayType) actualTypeArgument).getGenericComponentType();
                while (componentType instanceof GenericArrayType) {
                    componentType = ((GenericArrayType) componentType).getGenericComponentType();
                }
                if (componentType instanceof TypeVariable) {
                    this.resolveTypeVariable(actualTypeArgument.getTypeName(), true, superType);
                    continue;
                }
            }
            Class<?> rawType = getRawType(actualTypeArgument);
            boolean isArray = actualTypeArgument instanceof GenericArrayType;
            QualifierGeneric nested = actualTypeArgument instanceof Class ? null : this.create(rawType, actualTypeArgument).resolve();
            Generic generic = superType == null ? new Generic(rawType, isArray) : new SuperGeneric(rawType, isArray, superType);
            if (this.genericInfo.containsKey(generic)) {
                generic.incrementIndex();
            }
            this.genericInfo.put(generic, nested);
        }
    }

    /*---------------------------------------------------- 静态方法 ----------------------------------------------------*/

    public static QualifierGeneric from(Class<?> clazz) {
        return new QualifierGeneric(clazz).resolve();
    }

    public static QualifierGeneric from(ParameterizedTypeImpl parameterizedType) {
        return new QualifierGeneric(parameterizedType).resolve();
    }

    public static QualifierGeneric from(Field field) {
        return new QualifierGeneric(field.getType(), field.getGenericType()).resolve();
    }

    public static QualifierGeneric from(Method method) {
        return new QualifierGeneric(method.getReturnType(), method.getGenericReturnType()).resolve();
    }

    public static QualifierGeneric from(Parameter parameter) {
        return new QualifierGeneric(parameter.getType(), parameter.getParameterizedType()).resolve();
    }
}
