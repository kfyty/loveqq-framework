package com.kfyty.core.utils;

import com.kfyty.core.exception.SupportException;
import com.kfyty.core.wrapper.WeakKey;
import com.kfyty.core.wrapper.function.Function3;
import com.kfyty.core.wrapper.function.Function4;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.CommonUtil.EMPTY_CLASS_ARRAY;
import static com.kfyty.core.utils.CommonUtil.EMPTY_OBJECT_ARRAY;
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
     * 属性缓存 key 生成器
     */
    private static final Function3<String, Class<?>, String, Boolean> fieldCacheKeyGenerator = (clazz, fieldName, containPrivate) -> clazz.getName() + "#" + fieldName + "@" + containPrivate;

    /**
     * 方法缓存 key 生成器
     */
    public static final Function4<String, Class<?>, String, Class<?>[], Boolean> methodCacheKeyGenerator = (clazz, methodName, parameterTypes, containPrivate) -> CommonUtil.format("{}#{}({})@{}", clazz.getName(), methodName, Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(",")), containPrivate);

    /**
     * 所有属性/方法缓存 key 生成器
     */
    public static final BiFunction<Class<?>, Boolean, String> fieldsMethodsCacheKeyGenerator = (clazz, containPrivate) -> clazz.getName() + "@" + containPrivate;

    /**
     * 属性缓存
     */
    private static final Map<WeakKey<String>, Field> fieldCache = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 方法缓存
     */
    private static final Map<WeakKey<String>, Method> methodCache = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 所有属性缓存
     */
    private static final Map<WeakKey<String>, Map<String, Field>> fieldMapCache = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 所有方法缓存
     */
    private static final Map<WeakKey<String>, List<Method>> methodsCache = Collections.synchronizedMap(new WeakHashMap<>());

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
                throw new SupportException("load class failed, class does not exist !", e);
            }
            log.error("load class failed, class does not exist: [{}]", className);
            return null;
        }
    }

    public static boolean isPresent(String className) {
        return load(className, false, false) != null;
    }

    public static boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean isStaticFinal(int modifiers) {
        return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    public static boolean hasAnyInterfaces(Class<?> clazz) {
        return clazz.isInterface() || clazz.getInterfaces().length > 0;
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return clazz.getInterfaces();
        }
        Class<?>[] clazzInterfaces = clazz.getInterfaces();
        Class<?>[] interfaces = new Class[clazzInterfaces.length + 1];
        System.arraycopy(clazzInterfaces, 0, interfaces, 0, clazzInterfaces.length);
        interfaces[clazzInterfaces.length] = clazz;
        return interfaces;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) {
        Map memberValues = (Map) ReflectUtil.getFieldValue(Proxy.getInvocationHandler(annotation), "memberValues");
        memberValues.put(annotationField, value);
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public static void makeAccessible(Executable executable) {
        if ((!Modifier.isPublic(executable.getModifiers()) || !Modifier.isPublic(executable.getDeclaringClass().getModifiers())) && !executable.isAccessible()) {
            executable.setAccessible(true);
        }
    }

    public static boolean isBaseDataType(Class<?> clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) ||
                Character.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) || LocalDate.class.isAssignableFrom(clazz) ||
                LocalTime.class.isAssignableFrom(clazz) || LocalDateTime.class.isAssignableFrom(clazz) ||
                Timestamp.class.isAssignableFrom(clazz) || Instant.class.isAssignableFrom(clazz);
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
        throw new SupportException(CommonUtil.format("cannot instance for abstract class: [{}]", clazz));
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            makeAccessible(constructor);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, Map<Class<?>, Object> constructorArgs) {
        Object[] parameterClasses = CommonUtil.empty(constructorArgs) ? null : constructorArgs.keySet().toArray(EMPTY_CLASS_ARRAY);
        Object[] parameterValues = parameterClasses == null ? EMPTY_OBJECT_ARRAY : constructorArgs.values().toArray();
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
        throw new SupportException("can't find a suitable constructor !");
    }

    /*----------------------------------------- 构造器/属性/方法相关方法 -----------------------------------------*/

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, getField(obj.getClass(), fieldName));
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        setFieldValue(obj, field, value, true);
    }

    public static void setFieldValue(Object obj, Field field, Object value, boolean useSetter) {
        try {
            if (obj == null || !useSetter) {
                makeAccessible(field);
                field.set(obj, value);
                return;
            }
            Method method = getMethod(obj.getClass(), CommonUtil.getSetter(field.getName()), field.getType());
            if (method == null) {
                setFieldValue(obj, field, value, false);
                return;
            }
            invokeMethod(obj, method, value);
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

    public static Object getFieldValue(Object obj, Field field) {
        return getFieldValue(obj, field, true);
    }

    public static Object getFieldValue(Object obj, Field field, boolean useGetter) {
        try {
            if (obj == null || !useGetter) {
                makeAccessible(field);
                return field.get(obj);
            }
            return ofNullable(getMethod(obj.getClass(), CommonUtil.getGetter(field.getName())))
                    .map(method -> invokeMethod(obj, method))
                    .orElseGet(() -> getFieldValue(obj, field, false));
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

    public static Field getField(Class<?> clazz, String fieldName) {
        final String fieldMapKey = fieldsMethodsCacheKeyGenerator.apply(clazz, true);
        return ofNullable(fieldMapCache.get(new WeakKey<>(fieldMapKey)))
                .map(e -> e.get(fieldName))
                .orElseGet(() -> getField(clazz, fieldName, true));
    }

    public static Field getField(Class<?> clazz, String fieldName, boolean containPrivate) {
        final String key = fieldCacheKeyGenerator.apply(clazz, fieldName, containPrivate);
        return fieldCache.computeIfAbsent(new WeakKey<>(key), k -> {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                return getSuperField(clazz, fieldName, containPrivate);
            }
        });
    }

    public static Field getSuperField(Class<?> clazz, String fieldName, boolean containPrivate) {
        if (Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return null;
        }
        Field field = getField(clazz, fieldName, containPrivate);
        return field == null || !containPrivate && Modifier.isPrivate(field.getModifiers()) ? null : field;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return getMethod(clazz, methodName, false, parameterTypes);
    }

    public static Method getMethod(Class<?> clazz, String methodName, boolean containPrivate, Class<?>... parameterTypes) {
        final String key = methodCacheKeyGenerator.apply(clazz, methodName, parameterTypes, containPrivate);
        return methodCache.computeIfAbsent(new WeakKey<>(key), k -> {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                return getSuperMethod(clazz, methodName, containPrivate, parameterTypes);
            }
        });
    }

    public static Method getSuperMethod(Class<?> clazz, String methodName, boolean containPrivate, Class<?>... parameterTypes) {
        if (clazz == null || Object.class.equals(clazz)) {
            return null;
        }
        final Predicate<Method> methodPredicate = m -> containPrivate || !Modifier.isPrivate(m.getModifiers());
        return ofNullable(clazz.getSuperclass())
                .map(e -> getMethod(clazz.getSuperclass(), methodName, containPrivate, parameterTypes))
                .filter(methodPredicate)
                .orElseGet(() -> {
                    for (Class<?> clazzInterface : clazz.getInterfaces()) {
                        Method method = getMethod(clazzInterface, methodName, containPrivate, parameterTypes);
                        if (method != null) {
                            return methodPredicate.test(method) ? method : null;
                        }
                    }
                    return null;
                });
    }

    public static Method getSuperMethod(Method method) {
        return getSuperMethod(method.getDeclaringClass(), method.getName(), false, method.getParameterTypes());
    }

    public static boolean isSuperMethod(Method superMethod, Method method) {
        return !Modifier.isPrivate(superMethod.getModifiers()) && superMethod.getName().equals(method.getName()) && Arrays.equals(superMethod.getParameterTypes(), method.getParameterTypes());
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        return getConstructor(clazz, false, parameterTypes);
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<?> clazz, boolean containPrivate, Class<?>... parameterTypes) {
        try {
            return (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return getSuperConstructor(clazz, containPrivate, parameterTypes);
        }
    }

    public static <T> Constructor<T> getSuperConstructor(Class<?> clazz, boolean containPrivate, Class<?>... parameterTypes) {
        if (Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return null;
        }
        Constructor<T> constructor = getConstructor(clazz, containPrivate, parameterTypes);
        return constructor == null || !containPrivate && Modifier.isPrivate(constructor.getModifiers()) ? null : constructor;
    }

    public static <T> Constructor<T> getSuperConstructor(Constructor<T> constructor) {
        return getSuperConstructor(constructor.getDeclaringClass(), false, constructor.getParameterTypes());
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

    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        return getFieldMap(clazz, true);
    }

    public static Map<String, Field> getFieldMap(Class<?> clazz, boolean containPrivate) {
        final String key = fieldsMethodsCacheKeyGenerator.apply(clazz, containPrivate);
        return fieldMapCache.computeIfAbsent(new WeakKey<>(key), k -> {
            Map<String, Field> map = new HashMap<>();
            map.putAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e)));
            getSuperFieldMap(clazz, containPrivate).forEach(map::putIfAbsent);
            return map;
        });
    }

    public static Map<String, Field> getSuperFieldMap(Class<?> clazz, boolean containPrivate) {
        if (Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return Collections.emptyMap();
        }
        return getFieldMap(clazz, containPrivate).values().stream().filter(e -> containPrivate || !Modifier.isPrivate(e.getModifiers())).collect(Collectors.toMap(Field::getName, e -> e));
    }

    public static List<Method> getMethods(Class<?> clazz) {
        return getMethods(clazz, false);
    }

    public static List<Method> getMethods(Class<?> clazz, boolean containPrivate) {
        final String key = fieldsMethodsCacheKeyGenerator.apply(clazz, containPrivate);
        return methodsCache.computeIfAbsent(new WeakKey<>(key), k -> {
            List<Method> list = Arrays.stream(clazz.getDeclaredMethods()).filter(e -> !e.isBridge()).collect(Collectors.toList());
            list.addAll(getSuperMethods(clazz, containPrivate).stream().filter(superMethod -> list.stream().noneMatch(e -> isSuperMethod(superMethod, e))).collect(Collectors.toList()));
            return list;
        });
    }

    public static List<Method> getSuperMethods(Class<?> clazz, boolean containPrivate) {
        if (clazz == null || Object.class.equals(clazz)) {
            return Collections.emptyList();
        }
        Predicate<Method> methodPredicate = e -> !e.isBridge() && (containPrivate || !Modifier.isPrivate(e.getModifiers()));
        List<Method> list = Arrays.stream(clazz.getInterfaces()).flatMap(e -> getMethods(e, containPrivate).stream()).filter(methodPredicate).collect(Collectors.toList());
        if (clazz.getSuperclass() != null) {
            list.addAll(getMethods(clazz.getSuperclass(), containPrivate).stream().filter(methodPredicate).collect(Collectors.toList()));
        }
        return list;
    }

    /*--------------------------------------------- 父类泛型相关方法 ---------------------------------------------*/

    /**
     * 获取继承的父类或父接口的泛型
     * 将会过滤掉非参数化类型的父类或者父接口
     *
     * @param clazz 子类
     * @return 父类或父接口泛型
     */
    public static List<Type> getGenerics(Class<?> clazz) {
        List<Type> superGenericType = new ArrayList<>(4);
        ofNullable(clazz.getGenericSuperclass()).filter(e -> !(e instanceof Class)).ifPresent(superGenericType::add);
        superGenericType.addAll(Arrays.stream(clazz.getGenericInterfaces()).filter(e -> !(e instanceof Class)).collect(Collectors.toList()));
        return superGenericType;
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
        return getSuperGeneric(clazz, c -> true, genericIndex);
    }

    /**
     * 根据父泛型匹配过滤器获取泛型类型
     * 默认泛型索引为 0，即第一个泛型类型
     *
     * @param clazz              子类
     * @param superGenericFilter 父类或父接口泛型匹配过滤器
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, Predicate<Type> superGenericFilter) {
        return getSuperGeneric(clazz, 0, superGenericFilter);
    }

    /**
     * 根据父泛型匹配过滤器获取泛型类型
     * 如果匹配的父类不具有泛型，则尝试获取第一个继承的父类或父接口泛型的泛型类型
     *
     * @param clazz               子类
     * @param superClassPredicate 父类类型过滤器
     * @param genericIndex        泛型索引
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, Predicate<Class<?>> superClassPredicate, int genericIndex) {
        return getSuperGeneric(clazz, superClassPredicate, genericIndex, 0, null);
    }

    /**
     * 根据父泛型匹配过滤器获取泛型类型
     *
     * @param clazz              子类
     * @param genericIndex       泛型索引
     * @param superGenericFilter 父类或父接口泛型匹配过滤器
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex, Predicate<Type> superGenericFilter) {
        return getSuperGeneric(clazz, c -> true, genericIndex, -1, superGenericFilter);
    }

    /**
     * 获取父类或父接口的泛型类型
     *
     * @param clazz              子类
     * @param superClassFilter   父类类型过滤器，将会一直测试 clazz.getSuperclass()，直到测试通过或者为空
     * @param genericIndex       泛型索引，ParameterizedType.getActualTypeArguments() 返回值索引
     * @param superGenericIndex  继承的父类或父接口泛型索引，由 getGenerics() 方法返回
     * @param superGenericFilter 父类或父接口泛型匹配过滤器，将会一直测试 getGenerics()，直到测试通过。superGenericIndex < 0 且非空时有效
     * @see ReflectUtil#getGenerics(Class)
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, Predicate<Class<?>> superClassFilter, int genericIndex, int superGenericIndex, Predicate<Type> superGenericFilter) {
        Objects.requireNonNull(clazz);
        final Class<?> source = clazz;
        while (clazz.getSuperclass() != null && !superClassFilter.test(clazz.getSuperclass())) {
            clazz = clazz.getSuperclass();
        }
        Type genericSuperclass = getGenericSuperclass(clazz);
        if (superGenericFilter != null || !(genericSuperclass instanceof ParameterizedType)) {
            List<Type> generics = getGenerics(clazz);
            if (superGenericIndex > -1) {
                genericSuperclass = generics.get(superGenericIndex);
            } else if (superGenericFilter != null) {
                Optional<Type> filterInterface = Optional.empty();
                while (CommonUtil.notEmpty(generics) && !(filterInterface = generics.stream().filter(superGenericFilter).findAny()).isPresent()) {
                    generics = generics.stream().filter(e -> e instanceof ParameterizedType).map(e -> ((ParameterizedType) e).getRawType()).filter(e -> e instanceof Class).flatMap(e -> getGenerics((Class<?>) e).stream()).collect(Collectors.toList());
                }
                if (!filterInterface.isPresent()) {
                    throw new SupportException("parent generic match failed !");
                }
                genericSuperclass = filterInterface.get();
            }
        }
        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException(clazz.getName() + " does not contain generic types !");
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if (actualTypeArguments[genericIndex] instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actualTypeArguments[genericIndex]).getRawType();
        }
        Type type = actualTypeArguments[genericIndex];
        if (type instanceof TypeVariable) {
            for (Type generic : getGenerics(source)) {
                try {
                    return getActualGenericType(getTypeVariableName(type), generic);
                } catch (SupportException e) {
                    log.warn(e.getMessage());
                }
            }
            throw new SupportException(source.getName() + " does not contain generic types !");
        }
        return (Class<?>) type;
    }

    public static Type getGenericSuperclass(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        Type genericSuperclass = clazz.getGenericSuperclass();
        while (genericSuperclass != null && !Objects.equals(superClass, Object.class) && !(genericSuperclass instanceof ParameterizedType)) {
            clazz = superClass;
            superClass = clazz.getSuperclass();
            genericSuperclass = clazz.getGenericSuperclass();
        }
        return genericSuperclass;
    }

    public static Class<?> getActualGenericType(String typeVariable, Class<?> clazz) {
        return getActualGenericType(typeVariable, getGenericSuperclass(clazz));
    }

    public static Class<?> getActualGenericType(String typeVariable, Type genericSuperclass) {
        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException("unable to get the parent generic type !");
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        TypeVariable<?>[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (typeVariable.equals(typeParameters[i].getName())) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[i];
            }
        }
        Type parent = ((ParameterizedType) genericSuperclass).getRawType();
        if (parent instanceof Class) {
            return getActualGenericType(typeVariable, (Class<?>) parent);
        }
        throw new SupportException("can't find actual generic type !");
    }

    /*--------------------------------------------- 其他方法 ---------------------------------------------*/

    /**
     * 根据属性参数，解析嵌套属性的类型
     *
     * @param param 属性参数 eg: obj.field
     * @param root  包含 obj 属性的对象
     */
    public static Class<?> parseFieldType(String param, Class<?> root) {
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
    public static Object parseValue(String param, Object obj) {
        String[] fields = param.split("\\.");
        for (String field : fields) {
            obj = getFieldValue(obj, field);
        }
        return obj;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 parseValue() 过程相反
     *
     * @param param 属性参数 eg: obj.field
     * @param obj   包含 obj 属性的对象
     * @param value 属性对象 obj 中的 field 属性的值
     */
    public static void setNestedFieldValue(String param, Object obj, Object value) {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for (int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if (i == fields.length - 1) {
                setFieldValue(obj, field, value);
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

    /**
     * 获取泛型的原始类型
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isArray() ? clazz.getComponentType() : clazz;
        }
        if (type instanceof GenericArrayType) {
            return getRawType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return getRawType(CommonUtil.empty(wildcardType.getLowerBounds()) ? wildcardType.getUpperBounds()[0] : wildcardType.getLowerBounds()[0]);
        }
        throw new SupportException("unable to get the raw type: " + type);
    }

    public static String getTypeVariableName(Type type) {
        if (type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).getName();
        }
        throw new SupportException("unable to get the type variable: " + type);
    }
}
