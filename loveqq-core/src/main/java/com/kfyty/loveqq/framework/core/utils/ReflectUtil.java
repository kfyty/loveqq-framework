package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_OBJECT_ARRAY;
import static java.util.Optional.ofNullable;

/**
 * 描述: 反射工具
 *
 * @author kfyty725
 * @date 2021/6/3 10:01
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class ReflectUtil {
    /**
     * 反射数据过滤器
     */
    private static final Predicate<Member> REFLECT_DATA_FILTER = (member) -> member != null && ((member.getModifiers() & 0x00001000) == 0) && ((member.getModifiers() & 0x00000040) == 0);

    /**
     * 属性集合提供者
     */
    private static final Function<Class<?>, Field[]> FIELDS_SUPPLIER = clazz -> {
        Field[] fields = clazz.getDeclaredFields();
        if (clazz == Object.class || clazz.getSuperclass() == null) {
            return fields;
        }
        Field[] superFields = getFields(clazz.getSuperclass());
        Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity()));
        for (Field superField : superFields) {
            fieldMap.putIfAbsent(superField.getName(), superField);
        }
        return fieldMap.values().toArray(new Field[0]);
    };

    /**
     * 方法集合提供者
     */
    private static final Function<Class<?>, Method[]> METHODS_SUPPLIER = clazz -> {
        Method[] methods = clazz.getDeclaredMethods();
        if (clazz == Object.class) {
            return Arrays.stream(methods).filter(REFLECT_DATA_FILTER).toArray(Method[]::new);
        }
        List<Method> methodList = new ArrayList<>(Arrays.asList(methods));
        if (clazz.getSuperclass() != null) {
            mergeSuperMethod(methodList, getMethods(clazz.getSuperclass()));
        }
        for (Class<?> interfaces : clazz.getInterfaces()) {
            mergeSuperMethod(methodList, getMethods(interfaces));
        }
        return methodList.stream().filter(REFLECT_DATA_FILTER).toArray(Method[]::new);
    };

    /**
     * 所有属性缓存
     */
    private static final Map<Class<?>, Field[]> FIELD_MAP_CACHE = new WeakConcurrentHashMap<>(256);

    /**
     * 所有方法缓存
     */
    private static final Map<Class<?>, Method[]> METHOD_MAP_CACHE = new WeakConcurrentHashMap<>(256);

    /*------------------------------------------------ 基础方法 ------------------------------------------------*/

    public static Class<?> load(String className) {
        return load(className, true);
    }

    public static Class<?> load(String className, boolean initialize) {
        return load(className, initialize, ClassLoaderUtil.classLoader(ReflectUtil.class));
    }

    public static Class<?> load(String className, boolean initialize, boolean throwIfFailed) {
        return load(className, initialize, ClassLoaderUtil.classLoader(ReflectUtil.class), throwIfFailed);
    }

    public static Class<?> load(String className, boolean initialize, ClassLoader classLoader) {
        return load(className, initialize, classLoader, true);
    }

    public static Class<?> load(String className, boolean initialize, ClassLoader classLoader, boolean throwIfFailed) {
        try {
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e) {
            if (throwIfFailed) {
                throw new ResolvableException("load class failed, class does not exist !", e);
            }
            log.error("load class failed, class does not exist: [{}]", className);
            return null;
        }
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className, false, ClassLoaderUtil.classLoader(ReflectUtil.class));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean isStaticFinal(int modifiers) {
        return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    public static boolean isEquals(Method method) {
        return method.getName().equals("equals") && method.getParameterCount() == 1 && method.getReturnType() == boolean.class;
    }

    public static boolean isHashCode(Method method) {
        return method.getName().equals("hashCode") && method.getParameterCount() == 0 && method.getReturnType() == int.class;
    }

    public static boolean isToString(Method method) {
        return method.getName().equals("toString") && method.getParameterCount() == 0 && method.getReturnType() == String.class;
    }

    public static boolean isEqualsHashCodeToString(Method method) {
        return isEquals(method) || isHashCode(method) || isToString(method);
    }

    public static boolean hasAnyInterfaces(Class<?> clazz) {
        return getInterfaces(clazz).length > 0;
    }

    public static boolean hasAnyInterfaces(Class<?>[] classes, Predicate<Class<?>> test) {
        if (CommonUtil.notEmpty(classes)) {
            for (Class<?> clazz : classes) {
                if (test.test(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        if (clazz.isInterface()) {
            return new Class<?>[]{clazz};
        }
        Set<Class<?>> interfaces = new HashSet<>();
        while (clazz != null && clazz != Object.class) {
            Class<?>[] classInterfaces = clazz.getInterfaces();
            Collections.addAll(interfaces, classInterfaces);
            clazz = clazz.getSuperclass();
        }
        return interfaces.toArray(CommonUtil.EMPTY_CLASS_ARRAY);
    }

    public static Field makeAccessible(Field field) {
        if (!field.isAccessible() && (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()))) {
            field.setAccessible(true);
        }
        return field;
    }

    public static void makeAccessible(Executable executable) {
        if (!executable.isAccessible() && (!Modifier.isPublic(executable.getModifiers()) || !Modifier.isPublic(executable.getDeclaringClass().getModifiers()))) {
            executable.setAccessible(true);
        }
    }

    public static boolean isBaseDataType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        return ConverterUtil.getTypeConverters().keySet().stream().map(Pair::getValue).anyMatch(e -> e == clazz);
    }

    /*--------------------------------------------- 创建实例相关方法 ---------------------------------------------*/

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {
        if (!isAbstract(clazz)) {
            return newInstance(searchSuitableConstructor(clazz));
        }
        if (clazz.isArray()) {
            return (T) Array.newInstance(clazz.getComponentType(), 0);
        }
        if (SortedSet.class.isAssignableFrom(clazz)) {
            return (T) new TreeSet<>();
        }
        if (Set.class.isAssignableFrom(clazz)) {
            return (T) new HashSet<>();
        }
        if (Queue.class.isAssignableFrom(clazz)) {
            return (T) new ArrayDeque<>();
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return (T) new ArrayList<>();
        }
        if (ConcurrentNavigableMap.class.isAssignableFrom(clazz)) {
            return (T) new ConcurrentSkipListMap<>();
        }
        if (ConcurrentMap.class.isAssignableFrom(clazz)) {
            return (T) new ConcurrentHashMap<>();
        }
        if (SortedMap.class.isAssignableFrom(clazz)) {
            return (T) new TreeMap<>();
        }
        if (Map.class.isAssignableFrom(clazz) && !Properties.class.isAssignableFrom(clazz)) {
            return (T) new HashMap<>();
        }
        throw new ResolvableException(CommonUtil.format("cannot instance for abstract class: [{}]", clazz));
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            makeAccessible(constructor);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, List<Pair<Class<?>, Object>> constructorArgs) {
        Object[] parameterClasses = CommonUtil.empty(constructorArgs) ? null : constructorArgs.stream().map(Pair::getKey).toArray(Class[]::new);
        Object[] parameterValues = parameterClasses == null ? EMPTY_OBJECT_ARRAY : constructorArgs.stream().map(Pair::getValue).toArray();
        Predicate<Constructor<T>> constructorPredicate = parameterClasses == null ? null : c -> Arrays.equals(parameterClasses, c.getParameterTypes());
        return newInstance(searchSuitableConstructor(clazz, constructorPredicate), parameterValues);
    }

    public static <T> Constructor<T> searchSuitableConstructor(Class<T> clazz) {
        return searchSuitableConstructor(clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> searchSuitableConstructor(Class<T> clazz, Predicate<Constructor<T>> constructorPredicate) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (CommonUtil.size(constructors) == 1) {
            return (Constructor<T>) constructors[0];
        }
        Constructor<?> noParameterConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                noParameterConstructor = constructor;
            }
            if (constructorPredicate != null && constructorPredicate.test((Constructor<T>) constructor)) {
                return (Constructor<T>) constructor;
            }
        }
        if (noParameterConstructor != null) {
            return (Constructor<T>) noParameterConstructor;
        }
        throw new ResolvableException("Can't find a suitable constructor: " + clazz);
    }

    /*----------------------------------------- 构造器/属性/方法相关方法 -----------------------------------------*/

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, getField(obj.getClass(), fieldName), value);
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        setFieldValue(obj, field, value, true);
    }

    public static void setFieldValue(Object obj, Field field, Object value, boolean useSetter) {
        try {
            if (obj == null || !useSetter) {
                makeAccessible(field).set(obj, value);
                return;
            }
            Method setter = getMethod(obj.getClass(), CommonUtil.getSetter(field.getName()), field.getType());
            if (setter == null) {
                makeAccessible(field).set(obj, value);
                return;
            }
            invokeMethod(obj, setter, value);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static void setFinalFieldValue(Object obj, Field field, Object value) {
        int modifiers = field.getModifiers();
        Field modifiersField = ReflectUtil.getField(Field.class, "modifiers");
        setFieldValue(field, modifiersField, field.getModifiers() & ~Modifier.FINAL);
        setFieldValue(obj, field, value);
        setFieldValue(field, modifiersField, modifiers);
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, getField(obj.getClass(), fieldName));
    }

    public static Object getFieldValue(Object obj, Field field) {
        return getFieldValue(obj, field, true);
    }

    public static Object getFieldValue(Object obj, Field field, boolean useGetter) {
        try {
            if (obj == null || !useGetter) {
                makeAccessible(field);
                return field.get(obj);
            }
            Method getter = getMethod(obj.getClass(), CommonUtil.getGetter(field.getName()));
            if (getter == null) {
                return getFieldValue(obj, field, false);
            }
            return invokeMethod(obj, getter);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object obj, String methodName, Object... args) {
        Class<?>[] classes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        return (T) invokeMethod(obj, getMethod(obj.getClass(), methodName, classes), args);
    }

    public static Object invokeMethod(Object obj, Method method, Object... args) {
        try {
            makeAccessible(method);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return getSuperConstructor(clazz, parameterTypes);
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        Field[] fields = getFields(clazz);
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        for (Method method : getMethods(clazz)) {
            if (methodName.equals(method.getName()) && (parameterTypes == null || Arrays.equals(parameterTypes, method.getParameterTypes()))) {
                return method;
            }
        }
        return null;
    }

    public static <T> Constructor<T> getSuperConstructor(Constructor<T> constructor) {
        return getSuperConstructor(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }

    public static <T> Constructor<T> getSuperConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        if (clazz == Object.class || (clazz = clazz.getSuperclass()) == null) {
            return null;
        }
        return getConstructor(clazz, parameterTypes);
    }

    public static Method getSuperMethod(Method method) {
        return getSuperMethod(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    public static Method getSuperMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }
        return ofNullable(clazz.getSuperclass())
                .map(e -> getMethod(e, methodName, parameterTypes))
                .orElseGet(
                        () -> Arrays.stream(clazz.getInterfaces())
                                .map(interfaces -> getMethod(interfaces, methodName, parameterTypes))
                                .filter(Objects::nonNull)
                                .findAny()
                                .orElse(null)
                );
    }

    public static Parameter getSuperParameters(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        if (executable instanceof Method) {
            Method method = (Method) executable;
            Method superMethod = getSuperMethod(method);
            return superMethod == null ? null : superMethod.getParameters()[(int) getFieldValue(parameter, "index")];
        }
        Constructor<?> constructor = (Constructor<?>) executable;
        Constructor<?> superConstructor = getSuperConstructor(constructor);
        return superConstructor == null ? null : superConstructor.getParameters()[(int) getFieldValue(parameter, "index")];
    }

    public static Field[] getFields(Class<?> clazz) {
        Field[] fields = FIELD_MAP_CACHE.get(clazz);
        if (fields == null) {
            fields = FIELDS_SUPPLIER.apply(clazz);
            FIELD_MAP_CACHE.putIfAbsent(clazz, fields);
        }
        return fields;
    }

    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        return Arrays.stream(getFields(clazz)).collect(Collectors.toMap(Field::getName, Function.identity()));
    }

    public static Method[] getMethods(Class<?> clazz) {
        Method[] methods = METHOD_MAP_CACHE.get(clazz);
        if (methods == null) {
            methods = METHODS_SUPPLIER.apply(clazz);
            METHOD_MAP_CACHE.putIfAbsent(clazz, methods);
        }
        return methods;
    }

    public static void mergeSuperMethod(List<Method> methods, Method[] superMethods) {
        loop:
        for (Method superMethod : superMethods) {
            for (Method method : methods) {
                if (isSuperMethod(superMethod, method)) {
                    continue loop;
                }
            }
            methods.add(superMethod);
        }
    }

    public static boolean isSuperMethod(Method superMethod, Method method) {
        return !Modifier.isPrivate(superMethod.getModifiers()) &&
                superMethod.getName().equals(method.getName()) &&
                Arrays.equals(superMethod.getParameterTypes(), method.getParameterTypes());
    }

    /*--------------------------------------------- 父类泛型相关方法 ---------------------------------------------*/

    /**
     * 获取继承的父类或父接口的泛型
     *
     * @param clazz 子类
     * @return 父类或父接口泛型
     */
    public static Type[] getSuperGenerics(Class<?> clazz) {
        Type superclass = clazz.getGenericSuperclass();
        Type[] interfaces = clazz.getGenericInterfaces();
        Type[] retValue = new Type[(superclass == null ? 0 : 1) + interfaces.length];
        System.arraycopy(interfaces, 0, retValue, 0, interfaces.length);
        if (superclass != null) {
            retValue[retValue.length - 1] = superclass;
        }
        return retValue;
    }

    /**
     * 获取父类或父接口的泛型类型
     * 如果父类不具有泛型，则尝试获取第一个继承的父类或父接口泛型的泛型类型
     * 默认泛型索引为 0，即第一个泛型类型
     *
     * @param clazz 子类
     */
    public static Class<?> getSuperGeneric(Class<?> clazz) {
        return getSuperGeneric(clazz, 0);
    }

    /**
     * 获取父类或父接口的泛型类型
     * 如果父类不具有泛型，则尝试获取第一个继承的父类或父接口泛型的泛型类型
     *
     * @param clazz        子类
     * @param genericIndex 泛型索引
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex) {
        return getSuperGeneric(clazz, clazz, genericIndex, null);
    }

    /**
     * 根据父泛型匹配过滤器获取泛型类型
     * 默认泛型索引为 0，即第一个泛型类型
     *
     * @param clazz              子类
     * @param superGenericFilter 父类或父接口泛型匹配过滤器
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, Predicate<ParameterizedType> superGenericFilter) {
        return getSuperGeneric(clazz, clazz, 0, superGenericFilter);
    }

    /**
     * 根据父泛型匹配过滤器获取泛型类型
     *
     * @param clazz              子类
     * @param genericIndex       泛型索引
     * @param superGenericFilter 父类或父接口泛型匹配过滤器
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex, Predicate<ParameterizedType> superGenericFilter) {
        return getSuperGeneric(clazz, clazz, genericIndex, superGenericFilter);
    }

    /**
     * 获取父类或父接口的泛型类型
     *
     * @param clazz              目标类
     * @param type               可以解析目标类的实际类型
     * @param index              泛型索引，ParameterizedType.getActualTypeArguments() 返回值索引
     * @param superGenericFilter 父类或父接口泛型匹配过滤器
     * @see ReflectUtil#getSuperGenerics(Class)
     */
    public static Class<?> getSuperGeneric(final Class<?> clazz, Type type, int index, Predicate<ParameterizedType> superGenericFilter) {
        final List<Deque<QualifierGeneric[]>> stacks;
        final QualifierGeneric resolved = new QualifierGeneric(clazz, type).resolve();
        if (type instanceof ParameterizedType && superGenericFilter.test((ParameterizedType) type)) {
            stacks = Collections.singletonList(new ArrayDeque<>(2) {{
                push(new QualifierGeneric[]{resolved});
            }});
        } else {
            stacks = resolveSuperGeneric(clazz, superGenericFilter);
        }
        for (Deque<QualifierGeneric[]> stack : stacks) {
            while (!stack.isEmpty()) {
                QualifierGeneric[] generics = stack.pop();
                loop:
                for (QualifierGeneric pop : generics) {
                    Optional<Generic> any = pop.getGenericInfo().keySet().stream().skip(index).findAny();
                    if (!any.isPresent()) {
                        continue;
                    }
                    Generic generic = any.get();
                    if (!generic.isTypeVariable()) {
                        return generic.get();
                    }
                    // 索引匹配，然后一级级的向上查询
                    TypeVariable<? extends Class<?>>[] typeParameters = pop.getSourceType().getTypeParameters();
                    for (int i = 0; i < typeParameters.length; i++) {
                        if (typeParameters[i].getTypeName().equals(generic.getTypeVariable())) {
                            index = i;
                            break loop;
                        }
                    }
                }
            }
            Optional<Generic> generic = resolved.getGenericInfo().keySet().stream().skip(index).findAny();
            if (generic.isPresent()) {
                return generic.get().get();
            }
        }
        throw new ResolvableException("Unable to resolve generic of type: " + clazz);
    }

    public static String getTypeVariableName(Type type) {
        if (type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).getName();
        }
        throw new ResolvableException("Unable to get the type variable: " + type);
    }

    /**
     * 构建父泛型路径
     * 内部的每一个元素，都是从解析类型到最终类型的继承路线，由于接口可以实现多个，所以才是数组类型
     *
     * @param clazz              目标 class
     * @param superGenericFilter 父类型过滤器，匹配时结束向上查询
     * @return 父泛型路径
     */
    public static List<Deque<QualifierGeneric[]>> resolveSuperGeneric(Class<?> clazz, Predicate<ParameterizedType> superGenericFilter) {
        List<QualifierGeneric[]> container = resolveSuperGeneric(clazz, superGenericFilter, new LinkedList<>());
        return resolveSuperGenericPath(clazz, container, new ArrayDeque<>(), new LinkedList<>());
    }

    private static List<Deque<QualifierGeneric[]>> resolveSuperGenericPath(Class<?> clazz, List<QualifierGeneric[]> container, Deque<QualifierGeneric[]> stack, List<Deque<QualifierGeneric[]>> paths) {
        QualifierGeneric[] children = container.stream().filter(e -> e[0].getSourceType() == clazz).findFirst().orElse(null);
        if (children == null) {
            paths.add(new ArrayDeque<>(stack));
        } else {
            stack.push(children);
            for (QualifierGeneric child : children) {
                Class<?> rawType = child.getRawType();
                if (rawType != Object.class) {
                    resolveSuperGenericPath(rawType, container, stack, paths);
                }
            }
            stack.pop();
        }
        return paths;
    }

    private static List<QualifierGeneric[]> resolveSuperGeneric(Class<?> clazz, Predicate<ParameterizedType> superGenericFilter, List<QualifierGeneric[]> stack) {
        Type[] superGenerics = getSuperGenerics(clazz);
        if (superGenerics.length < 1) {
            return stack;
        }
        QualifierGeneric[] parents = new QualifierGeneric[superGenerics.length];
        for (int i = 0; i < superGenerics.length; i++) {
            parents[i] = new QualifierGeneric(clazz, superGenerics[i]).resolve();
        }
        stack.add(parents);
        if (superGenericFilter != null) {
            if (Arrays.stream(superGenerics).anyMatch(e -> e instanceof ParameterizedType && superGenericFilter.test((ParameterizedType) e))) {
                return stack;
            }
        }
        for (Type generic : superGenerics) {
            resolveSuperGeneric(QualifierGeneric.getRawType(generic), superGenericFilter, stack);
        }
        return stack;
    }

    /*--------------------------------------------- 其他方法 ---------------------------------------------*/

    /**
     * 根据属性参数，解析嵌套属性的类型
     *
     * @param param 属性参数 eg: obj.field
     * @param root  包含 obj 属性的对象
     */
    public static Class<?> resolveFieldType(String param, Class<?> root) {
        Class<?> clazz = root;
        String[] fields = param.split("\\.");
        for (int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if (i == fields.length - 1) {
                return field.getType();
            }
            clazz = field.getType();
        }
        return root;
    }

    /**
     * 根据属性参数，解析出 object 中对应的属性值
     *
     * @param param 属性参数，eg: obj.value
     * @param obj   包含 obj 属性的对象
     * @return 属性值
     */
    public static Object resolveValue(String param, Object obj) {
        String[] fields = param.split("\\.");
        for (String field : fields) {
            obj = getFieldValue(obj, field);
            if (obj == null) {
                return null;
            }
        }
        return obj;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 {@link ReflectUtil#resolveValue(String, Object)} 过程相反
     *
     * @param param 属性参数 eg: obj.field
     * @param obj   包含 obj 属性的对象
     * @param value 属性对象 obj 中的 field 属性的值
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setNestedFieldValue(String param, Object obj, Object value) {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for (int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if (i == fields.length - 1) {
                Converter converter = ConverterUtil.getTypeConverter(value.getClass(), field.getType());
                setFieldValue(obj, field, converter == null ? value : converter.apply(value));
                break;
            }
            Object fieldValue = getFieldValue(obj, field);
            if (fieldValue != null) {
                obj = fieldValue;
                clazz = field.getType();
                continue;
            }
            setFieldValue(obj, field, (obj = newInstance(field.getType())));
            clazz = field.getType();
        }
    }
}
