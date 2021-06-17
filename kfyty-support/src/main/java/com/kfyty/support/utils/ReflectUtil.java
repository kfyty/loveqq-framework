package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    public static boolean isJdkProxy(Object instance) {
        return Proxy.isProxyClass(instance.getClass());
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
        return getSuperGeneric(clazz, genericIndex, 0);
    }

    /**
     * 获取父类或父接口的泛型类型
     * @param clazz 子类
     * @param genericIndex 泛型索引
     * @param interfaceIndex 实现的接口索引
     */
    public static Class<?> getSuperGeneric(Class<?> clazz, int genericIndex, int interfaceIndex) {
        Objects.requireNonNull(clazz);
        Type genericSuperclass = clazz.getGenericSuperclass();
        if(genericSuperclass == null || genericSuperclass.equals(Object.class)) {
            genericSuperclass = clazz.getGenericInterfaces()[interfaceIndex];
        }
        if(!(genericSuperclass instanceof ParameterizedType)) {
            throw new SupportException(clazz.getName() + "does not contain generic types !");
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        return (Class<?>) actualTypeArguments[genericIndex];
    }

    public static Class<?> getActualFieldType(Class<?> clazz, Field field) {
        String genericSignature = (String) invokeSimpleMethod(field, "getGenericSignature");
        if(genericSignature == null) {
            return field.getType();
        }
        Type genericSuperclass = clazz.getGenericSuperclass();
        if(!(genericSuperclass instanceof ParameterizedType)) {
            return field.getType();
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        TypeVariable<?>[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if(genericSignature.contains(typeParameters[i].getName())) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[i];
            }
        }
        return field.getType();
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

    public static Object invokeSimpleMethod(Object obj, String methodName, Object ... args) {
        return invokeMethod(obj, getMethod(obj.getClass(), methodName), args);
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
        return getSuperMethod(method.getDeclaringClass(), method.getName(), false, method.getParameterTypes());
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
        return getFieldMap(clazz, false);
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
}
