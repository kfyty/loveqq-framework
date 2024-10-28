package com.kfyty.loveqq.framework.core.generic;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getTypeVariableName;

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
     * 源类型，即解析的泛型所在的，具有真实类型的类型
     */
    protected final Class<?> sourceType;

    /**
     * 解析的目标类型
     *
     * @see Field#getGenericType()
     * @see Method#getGenericReturnType()
     * @see Parameter#getParameterizedType()
     */
    protected final Type resolveType;

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
        this(sourceType, sourceType);
    }

    /**
     * 构建一个没有原始类型的泛型的 QualifierGeneric
     *
     * @param genericType 类型
     */
    public QualifierGeneric(Type genericType) {
        this(getRawType(genericType), genericType);
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
        return this.resolveGenericType(this.resolveType);
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
        return generic.isAssignableFrom(this.sourceType) || generic.isAssignableFrom(getRawType(this.resolveType));
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
        if (this.size() < 1) {
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
        for (Type type : ReflectUtil.getSuperGenerics(clazz)) {
            if (type instanceof ParameterizedType) {
                this.resolveParameterizedType((ParameterizedType) type, getRawType(type));
            }
        }
    }

    protected void resolveGenericArrayType(GenericArrayType type) {
        Set<Generic> cache = new HashSet<>(this.genericInfo.keySet());
        this.resolveGenericType(type.getGenericComponentType());

        // 必须先将修改的 key 移除，再重新放入，否则 get 取不到数据
        List<Map.Entry<Generic, QualifierGeneric>> changed = new LinkedList<>();
        for (Iterator<Map.Entry<Generic, QualifierGeneric>> i = this.genericInfo.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<Generic, QualifierGeneric> next = i.next();
            if (cache.contains(next.getKey())) {
                continue;
            }
            next.getKey().setArray(true);
            changed.add(next);
            i.remove();
        }
        changed.forEach(e -> this.genericInfo.put(e.getKey(), e.getValue()));
    }

    protected void resolveWildcardType(WildcardType wildcardType) {
        Type type = CommonUtil.empty(wildcardType.getLowerBounds()) ? wildcardType.getUpperBounds()[0] : wildcardType.getLowerBounds()[0];
        this.resolveGenericType(type);
    }

    protected void resolveTypeVariable(TypeVariable<?> typeVariable) {
        GenericDeclaration declaration = typeVariable.getGenericDeclaration();
        if (declaration instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaration;
            Type superType = this.resolveTypeVariableSuperType(this.sourceType, clazz);
            if (superType instanceof ParameterizedType) {
                int index = resolveTypeVariableIndex(typeVariable, clazz);
                this.resolveParameterizedType((ParameterizedType) superType, null, index);
                return;
            }
        }
        this.resolveTypeVariable(getTypeVariableName(typeVariable), false, null);
    }

    protected Type resolveTypeVariableSuperType(Class<?> sourceType, Class<?> clazz) {
        Class<?> superClass = sourceType;
        while (superClass != null && superClass != Object.class) {
            Class<?> temp = superClass.getSuperclass();
            if (temp == clazz) {
                return superClass.getGenericSuperclass();
            }
            superClass = temp;
        }
        Class<?>[] interfaces = sourceType.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] == clazz) {
                return sourceType.getGenericInterfaces()[i];
            }
            if (clazz.isAssignableFrom(interfaces[i])) {
                return resolveTypeVariableSuperType(interfaces[i], clazz);
            }
        }
        return clazz;
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
        resolveParameterizedType(type, superType, null);
    }

    protected void resolveParameterizedType(ParameterizedType type, Class<?> superType, Integer targetIndex) {
        int index = -1;
        Type[] actualTypeArguments = type.getActualTypeArguments();
        for (Type actualTypeArgument : actualTypeArguments) {
            index++;
            if (targetIndex != null && targetIndex != index) {
                continue;
            }
            if (actualTypeArgument instanceof TypeVariable) {
                this.resolveTypeVariable((TypeVariable<?>) actualTypeArgument);
                continue;
            }
            if (actualTypeArgument instanceof GenericArrayType) {
                this.resolveGenericArrayType((GenericArrayType) actualTypeArgument);
                continue;
            }
            Class<?> rawType = getRawType(actualTypeArgument);
            Generic generic = superType == null ? new Generic(rawType) : new SuperGeneric(rawType, superType);
            QualifierGeneric nested = actualTypeArgument instanceof Class ? null : this.create(this.sourceType, actualTypeArgument).resolve();
            if (this.genericInfo.containsKey(generic)) {
                generic.incrementIndex();
            }
            this.genericInfo.put(generic, nested);
        }
    }

    @Override
    public String toString() {
        return CommonUtil.format("sourceType={}, resolveType={}, generic={}", this.sourceType, this.resolveType, this.genericInfo);
    }

    /*---------------------------------------------------- 静态方法 ----------------------------------------------------*/

    public static QualifierGeneric from(Class<?> clazz) {
        return new QualifierGeneric(clazz).resolve();
    }

    public static QualifierGeneric from(Field field) {
        return new QualifierGeneric(field.getDeclaringClass(), field.getGenericType()).resolve();
    }

    public static QualifierGeneric from(Method method) {
        return new QualifierGeneric(method.getDeclaringClass(), method.getGenericReturnType()).resolve();
    }

    public static QualifierGeneric from(Parameter parameter) {
        return new QualifierGeneric(parameter.getDeclaringExecutable().getDeclaringClass(), parameter.getParameterizedType()).resolve();
    }

    public static QualifierGeneric from(ParameterizedType parameterizedType) {
        return new QualifierGeneric(parameterizedType).resolve();
    }

    public static QualifierGeneric from(Class<?> sourceType, Field field) {
        return new QualifierGeneric(sourceType, field.getGenericType()).resolve();
    }

    public static QualifierGeneric from(Class<?> sourceType, Method method) {
        return new QualifierGeneric(sourceType, method.getGenericReturnType()).resolve();
    }

    public static QualifierGeneric from(Class<?> sourceType, Parameter parameter) {
        return new QualifierGeneric(sourceType, parameter.getParameterizedType()).resolve();
    }

    public static int resolveTypeVariableIndex(TypeVariable<?> typeVariable, Class<?> declaration) {
        TypeVariable<? extends Class<?>>[] typeParameters = declaration.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (Objects.equals(typeParameters[i], typeVariable)) {
                return i;
            }
        }
        throw new ResolvableException("Resolve type variable failed: " + typeVariable);
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return getRawType(((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }
        if (type instanceof TypeVariable) {
            return Object.class;
        }
        throw new ResolvableException("Resolve raw type failed: " + type);
    }
}
