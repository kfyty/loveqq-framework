package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 描述: 反射工具
 *
 * @author kfyty725
 * @date 2021/6/3 10:01
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class ReflectUtil {

    public static boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean hasAnyInterfaces(Class<?> clazz) {
        return getInterfaces(clazz).length > 0;
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        if(!clazz.isInterface()) {
            return clazz.getInterfaces();
        }
        Class<?>[] clazzInterfaces = clazz.getInterfaces();
        Class<?>[] interfaces = new Class[clazzInterfaces.length + 1];
        System.arraycopy(clazzInterfaces, 0, interfaces, 0, clazzInterfaces.length);
        interfaces[clazzInterfaces.length] = clazz;
        return interfaces;
    }

    public static boolean isBaseDataType(Class<?> clazz) {
        return clazz.isPrimitive()                          ||
                Number.class.isAssignableFrom(clazz)        ||
                CharSequence.class.isAssignableFrom(clazz)  ||
                Character.class.isAssignableFrom(clazz)     ||
                Date.class.isAssignableFrom(clazz)          ||
                LocalTime.class.isAssignableFrom(clazz)     ||
                LocalDate.class.isAssignableFrom(clazz)     ||
                LocalDateTime.class.isAssignableFrom(clazz) ||
                Timestamp.class.isAssignableFrom(clazz)     ||
                Instant.class.isAssignableFrom(clazz);
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            if(!isAbstract(clazz)) {
                return newInstance(searchSuitableConstructor(clazz));
            }
            throw new SupportException(CommonUtil.format("cannot instance for abstract class: [{}]", clazz));
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object ... args) {
        try {
            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            Object value = constructor.newInstance(args);
            constructor.setAccessible(accessible);
            return (T) value;
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, Map<Class<?>, Object> constructorArgs) {
        try {
            Object[] parameterClasses = CommonUtil.empty(constructorArgs) ? null : constructorArgs.keySet().toArray(new Class[0]);
            Object[] parameterValues = parameterClasses == null ? new Object[0] : constructorArgs.values().toArray();
            Predicate<Constructor<T>> constructorPredicate = parameterClasses == null ? null : c -> Arrays.equals(parameterClasses, c.getParameterTypes());
            return newInstance(searchSuitableConstructor(clazz, constructorPredicate), parameterValues);
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static <T> Constructor<T> searchSuitableConstructor(Class<T> clazz) {
        return searchSuitableConstructor(clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> searchSuitableConstructor(Class<T> clazz, Predicate<Constructor<T>> constructorPredicate) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if(CommonUtil.size(constructors) == 1) {
            return (Constructor<T>) constructors[0];
        }
        Constructor<?> noParameterConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if(constructor.getParameterCount() == 0) {
                noParameterConstructor = constructor;
            }
            if(constructorPredicate != null && constructorPredicate.test((Constructor<T>) constructor)) {
                return (Constructor<T>) constructor;
            }
        }
        if(noParameterConstructor != null) {
            return (Constructor<T>) noParameterConstructor;
        }
        throw new SupportException("can't find a suitable constructor !");
    }

    public static Class<?> getSuperGeneric(Class<?> clazz) {
        return getSuperGeneric(clazz, 0);
    }

    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex) {
        return getSuperGeneric(clazz, genericIndex, 0, null);
    }

    /**
     * 获取父类或父接口的泛型类型
     * @param clazz 子类
     * @param genericIndex 泛型索引
     * @param interfaceIndex 实现的接口索引
     * @param interfaceFilter 接口匹配过滤器，interfaceIndex < 0 时有效
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex, int interfaceIndex, Predicate<Type> interfaceFilter) {
        Objects.requireNonNull(clazz);
        Type genericSuperclass = getGenericSuperclass(clazz);
        if(genericSuperclass == null || genericSuperclass.equals(Object.class) || !(genericSuperclass instanceof ParameterizedType)) {
            Type[] interfaces = clazz.getGenericInterfaces();
            if(interfaceIndex > -1) {
                genericSuperclass = interfaces[interfaceIndex];
            } else {
                Optional<Type> filterInterface = Arrays.stream(interfaces).filter(interfaceFilter).findAny();
                if(!filterInterface.isPresent()) {
                    throw new SupportException("parent interface match failed !");
                }
                genericSuperclass = filterInterface.get();
            }
        }
        if(!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException(clazz.getName() + " does not contain generic types !");
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if(actualTypeArguments[genericIndex] instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actualTypeArguments[genericIndex]).getRawType();
        }
        return (Class<?>) actualTypeArguments[genericIndex];
    }

    public static Type getGenericSuperclass(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        while (genericSuperclass != null && !Objects.equals(clazz.getSuperclass(), Object.class) && !(genericSuperclass instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
            genericSuperclass = clazz.getGenericSuperclass();
        }
        return genericSuperclass;
    }

    public static Class<?> getActualGenericType(Class<?> clazz, int index) {
        Type genericSuperclass = getGenericSuperclass(clazz);
        if(!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException("unable to get the parent generic type !");
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        return (Class<?>) parameterizedType.getActualTypeArguments()[index];
    }

    public static int getActualGenericIndex(Class<?> clazz, String typeVariable) {
        Type genericSuperclass = getGenericSuperclass(clazz);
        if(!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException("unable to get the parent generic type !");
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        TypeVariable<?>[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if(typeVariable.equals(typeParameters[i].getName())) {
                return i;
            }
        }
        throw new SupportException("can't find actual generic index !");
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, getField(obj.getClass(), fieldName));
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(accessible);
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static void setFinalFieldValue(Object obj, Field field, Object value) {
        try {
            int modifiers = field.getModifiers();
            Field modifiersField = ReflectUtil.getField(Field.class, "modifiers");
            setFieldValue(field, modifiersField, field.getModifiers() & ~Modifier.FINAL);
            setFieldValue(obj, field, value);
            setFieldValue(field, modifiersField, modifiers);
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static Object getFieldValue(Object obj, Field field) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object ret = field.get(obj);
            field.setAccessible(accessible);
            return ret;
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeSimpleMethod(Object obj, String methodName, Object ... args) {
        return (T) invokeMethod(obj, getMethod(obj.getClass(), methodName), args);
    }

    public static Object invokeMethod(Object obj, Method method, Object ... args) {
        try {
            boolean accessible = method.isAccessible();
            method.setAccessible(true);
            Object ret = method.invoke(obj, args);
            method.setAccessible(accessible);
            return ret;
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, false);
    }

    public static Field getField(Class<?> clazz, String fieldName, boolean containPrivate) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch(NoSuchFieldException e) {
            return getSuperField(clazz, fieldName, containPrivate);
        }
    }

    public static Field getSuperField(Class<?> clazz, String fieldName, boolean containPrivate) {
        if(Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            log.error("field does not exist: [{}] !", fieldName);
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return !containPrivate && Modifier.isPrivate(field.getModifiers()) ? null : field;
        } catch(NoSuchFieldException e) {
            return getSuperField(clazz, fieldName, containPrivate);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?> ... parameterTypes) {
        return getMethod(clazz, methodName, false, parameterTypes);
    }

    public static Method getMethod(Class<?> clazz, String methodName, boolean containPrivate, Class<?> ... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch(NoSuchMethodException e) {
            return getSuperMethod(clazz, methodName, containPrivate);
        }
    }

    public static Method getSuperMethod(Method method) {
        Method superMethod = getSuperMethod(method.getDeclaringClass(), method.getName(), false, method.getParameterTypes());
        if(superMethod != null) {
            return superMethod;
        }
        for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
            superMethod = getMethod(clazz, method.getName(), false, method.getParameterTypes());
            if(superMethod != null) {
                return superMethod;
            }
        }
        return null;
    }

    public static Method getSuperMethod(Class<?> clazz, String methodName, boolean containPrivate, Class<?> ... parameterTypes) {
        if(Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return null;
        }
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return !containPrivate && Modifier.isPrivate(method.getModifiers()) ? null : method;
        } catch(NoSuchMethodException e) {
            return getSuperMethod(clazz, methodName, containPrivate, parameterTypes);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?> ... parameterTypes) {
        return getConstructor(clazz, false, parameterTypes);
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<?> clazz, boolean containPrivate, Class<?> ... parameterTypes) {
        try {
            return (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
        } catch(NoSuchMethodException e) {
            return getSuperConstructor(clazz, containPrivate, parameterTypes);
        }
    }

    public static <T> Constructor<T> getSuperConstructor(Constructor<T> constructor) {
        return getSuperConstructor(constructor.getDeclaringClass(), false, constructor.getParameterTypes());
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getSuperConstructor(Class<?> clazz, boolean containPrivate, Class<?> ... parameterTypes) {
        if(Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return null;
        }
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            return !containPrivate && Modifier.isPrivate(constructor.getModifiers()) ? null : constructor;
        } catch(NoSuchMethodException e) {
            return getSuperConstructor(clazz, containPrivate, parameterTypes);
        }
    }

    public static Parameter getSuperParameters(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        if(executable instanceof Method) {
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
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getSuperFieldMap(clazz, containPrivate));
        return map;
    }

    public static Map<String, Field> getSuperFieldMap(Class<?> clazz, boolean containPrivate) {
        if(clazz == null || Object.class.equals(clazz) || (clazz = clazz.getSuperclass()) == null) {
            return new HashMap<>(0);
        }
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).filter(e -> containPrivate || !Modifier.isPrivate(e.getModifiers())).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getSuperFieldMap(clazz, containPrivate));
        return map;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        Map memberValues = (Map) ReflectUtil.getFieldValue(invocationHandler, "memberValues");
        memberValues.put(annotationField, value);
    }

    /**
     * 根据属性参数，解析嵌套属性的类型
     * @param param     属性参数 eg: obj.field
     * @param root       包含 obj 属性的对象
     */
    public static Class<?> parseFieldType(String param, Class<?> root) {
        Class<?> clazz = root;
        String[] fields = param.split("\\.");
        for(int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if(i == fields.length - 1) {
                return field.getType();
            }
            clazz = field.getType();
        }
        return root;
    }

    /**
     * 根据属性参数，解析出 object 中对应的属性值
     * @param param     属性参数，eg: obj.value
     * @param obj       包含 obj 属性的对象
     * @return          属性值
     */
    public static Object parseValue(String param, Object obj) {
        String[] fields = param.split("\\.");
        for (String field : fields) {
            obj = ReflectUtil.getFieldValue(obj, field);
        }
        return obj;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 parseValue() 过程相反
     * @param param     属性参数 eg: obj.field
     * @param obj       包含 obj 属性的对象
     * @param value     属性对象 obj 中的 field 属性的值
     */
    public static void setNestedFieldValue(String param, Object obj, Object value) {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for(int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if(i == fields.length - 1) {
                setFieldValue(obj, field, value);
                break;
            }
            Object fieldValue = getFieldValue(obj, field);
            if(fieldValue != null) {
                obj = fieldValue;
                clazz = field.getType();
                continue;
            }
            setFieldValue(obj, field, (obj = ReflectUtil.newInstance(field.getType())));
            clazz = field.getType();
        }
    }

    /**
     * 获取泛型的原始类型
     */
    public static Class<?> getRawType(Type type) {
        if(type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isArray() ? clazz.getComponentType() : clazz;
        }
        if(type instanceof GenericArrayType) {
            return getRawType(((GenericArrayType) type).getGenericComponentType());
        }
        if(type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if(type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return getRawType(CommonUtil.empty(wildcardType.getLowerBounds()) ? wildcardType.getUpperBounds()[0] : wildcardType.getLowerBounds()[0]);
        }
        throw new SupportException("unable to get the raw type !");
    }

    public static String getTypeVariableName(Type type) {
        if(type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).getName();
        }
        throw new SupportException("unable to get the type variable !");
    }
}
