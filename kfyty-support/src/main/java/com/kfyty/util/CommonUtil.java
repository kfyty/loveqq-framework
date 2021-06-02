package com.kfyty.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 功能描述: 通用工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/6/27 11:07
 * @since JDK 1.8
 */
@Slf4j
public class CommonUtil {
    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z0-9]*");

    public static boolean empty(String s) {
        return !Optional.ofNullable(s).filter(e -> e.trim().length() != 0).isPresent();
    }

    public static <T> boolean empty(T[] t) {
        return !Optional.ofNullable(t).filter(e -> e.length != 0).isPresent();
    }

    public static boolean empty(Collection<?> c) {
        return !Optional.ofNullable(c).filter(e -> !e.isEmpty()).isPresent();
    }

    public static boolean empty(Map<?, ?> m) {
        return !Optional.ofNullable(m).filter(e -> !e.isEmpty()).isPresent();
    }

    public static int size(Object obj) {
        if(obj == null) {
            return 0;
        }
        if(obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        if(obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if(obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }
        return 1;
    }

    public static String getStackTrace(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(throwable1 -> {
                    StringWriter stringWriter = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(stringWriter, true));
                    return stringWriter.toString();
                }).orElseThrow(() -> new NullPointerException("throwable is null"));
    }

    public static String convert2BeanName(Class<?> clazz) {
        return convert2BeanName(clazz.getSimpleName());
    }

    public static String convert2BeanName(String className) {
        if(className.length() > 1 && Character.isUpperCase(className.charAt(1))) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    public static String convert2Hump(String s) {
        return convert2Hump(s, false);
    }

    public static String convert2Hump(String s, boolean isClass) {
        if(empty(s)) {
            throw new NullPointerException("column is null");
        }
        s = s.contains("_") || UPPER_CASE_PATTERN.matcher(s).matches() ? s.toLowerCase() : s;
        while(s.contains("_")) {
            int index = s.indexOf('_');
            if(index == s.length() - 1) {
                break;
            }
            char ch = s.charAt(index + 1);
            s = s.replace("_" + ch, "" + Character.toUpperCase(ch));
        }
        return !isClass ? s : s.length() == 1 ? s.toUpperCase() : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String convert2Underline(String s) {
        return convert2Underline(s, true);
    }

    public static String convert2Underline(String s, boolean lower) {
        if(empty(s)) {
            throw new NullPointerException("field is null");
        }
        if(UPPER_CASE_PATTERN.matcher(s).matches()) {
            return lower ? s.toLowerCase() : s.toUpperCase();
        }
        char c = s.charAt(0);
        StringBuilder builder = new StringBuilder();
        builder.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : c);
        for(int i = 1; i < s.length(); i++) {
            c = s.charAt(i);
            if(Character.isUpperCase(c)) {
                builder.append("_").append(Character.toLowerCase(c));
                continue;
            }
            builder.append(c);
        }
        return lower ? builder.toString() : builder.toString().toUpperCase();
    }

    public static String convert2JdbcType(String dataBaseType) {
        if(dataBaseType.toLowerCase().contains("timestamp")) {
            return "TIMESTAMP";
        }
        switch (dataBaseType.toLowerCase()) {
            case "bit":
                return "BIT";
            case "smallint":
                return "SMALLINT";
            case "char":
            case "text":
            case "varchar":
            case "varchar2":
            case "nvarchar2":
                return "VARCHAR";
            case "clob":
                return "CLOB";
            case "nclob":
                return "NCLOB";
            case "longtext":
            case "long varchar":
                return "LONGVARCHAR";
            case "decimal":
                return "DECIMAL";
            case "bigint":
                return "BIGINT";
            case "long":
            case "number":
            case "numeric":
                return "NUMERIC";
            case "tinyint":
                return "TINYINT";
            case "int":
            case "integer":
                return "INTEGER";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            case "time":
            case "date":
                return "DATE";
            case "datetime":
            case "datetime2":
                return "TIMESTAMP";
            case "blob":
                return "BLOB";
            case "longblob":
            case "binary":
            case "varbinary":
                return "BINARY";
            default :
                log.warn("No jdbc type matched and instead of 'OTHER' !");
                return "OTHER";
        }
    }

    public static String convert2JavaType(String dataBaseType) {
        if(dataBaseType.toLowerCase().contains("timestamp")) {
            return "Date";
        }
        switch (dataBaseType.toLowerCase()) {
            case "bit":
                return "Byte";
            case "char":
            case "text":
            case "json":
            case "other":
            case "longtext":
            case "clob":
            case "nclob":
            case "varchar":
            case "varchar2":
            case "nvarchar2":
                return "String";
            case "decimal":
                return "BigDecimal";
            case "long":
            case "bigint":
            case "number":
            case "numeric":
                return "Long";
            case "tinyint":
            case "smallint":
            case "int":
            case "integer":
                return "Integer";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "time":
            case "date":
            case "datetime":
            case "datetime2":
                return "Date";
            case "blob":
            case "longblob":
            case "binary":
            case "varbinary":
                return "byte[]";
            default :
                return null;
        }
    }

    public static String fillString(String s, Object ... params) {
        int index = -1;
        int paramIndex = 0;
        StringBuilder sb = new StringBuilder(s);
        while((index = sb.indexOf("{}", index)) != -1) {
            sb.replace(index, index + 2, Optional.ofNullable(params[paramIndex++]).map(Object::toString).orElse(""));
        }
        return sb.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) throws Exception {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        Map memberValues = (Map) getFieldValue(invocationHandler, "memberValues");
        memberValues.put(annotationField, value);
    }

    public static List<Object> convert2List(Object value) {
        List<Object> list = new ArrayList<>();
        if(value instanceof Collection) {
            list.addAll((Collection<?>) value);
        } else if(value.getClass().isArray()) {
            list.addAll(Arrays.asList((Object[]) value));
        } else {
            log.error(": convert data error, parameter is not collection or array !");
            return null;
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> convert2Wrapper(Class<T> clazz) throws Exception {
        String type = clazz.getSimpleName();
        return Character.isUpperCase(type.charAt(0)) ? clazz :
                "int".equals(type) ? (Class<T>) Integer.class : (Class<T>) Class.forName("java.lang." + Character.toUpperCase(type.charAt(0)) + type.substring(1));
    }

    public static boolean isBaseDataType(Class<?> clazz) {
        return byte.class.isAssignableFrom(clazz)           ||
                short.class.isAssignableFrom(clazz)         ||
                int.class.isAssignableFrom(clazz)           ||
                long.class.isAssignableFrom(clazz)          ||
                float.class.isAssignableFrom(clazz)         ||
                double.class.isAssignableFrom(clazz)        ||
                Number.class.isAssignableFrom(clazz)        ||
                CharSequence.class.isAssignableFrom(clazz)  ||
                Date.class.isAssignableFrom(clazz);
    }

    /**
     * 简单的实例化
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            if(!CommonUtil.isAbstract(clazz)) {
                return newInstance(clazz.getDeclaredConstructor());
            }
            throw new RuntimeException(CommonUtil.fillString("cannot instance for abstract class: [{}]", clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor) {
        try {
            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            Object value = constructor.newInstance();
            constructor.setAccessible(accessible);
            return (T) value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if(genericSuperclass == null) {
            genericSuperclass = clazz.getGenericInterfaces()[interfaceIndex];
        }
        if(!(genericSuperclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(clazz.getName() + "does not contain generic types !");
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        return (Class<?>) actualTypeArguments[genericIndex];
    }

    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        return getFieldValue(obj, getField(obj.getClass(), fieldName));
    }

    public static void setFieldValue(Object obj, Field field, Object value) throws Exception {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(accessible);
    }

    public static Object getFieldValue(Object obj, Field field) throws Exception {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Object ret = field.get(obj);
        field.setAccessible(accessible);
        return ret;
    }

    public static Object invokeSimpleMethod(Object obj, String methodName, Object ... args) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName);
        return invokeMethod(obj, method, args);
    }

    public static Object invokeMethod(Object obj, Method method, Object ... args) throws Exception {
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object ret = method.invoke(obj, args);
        method.setAccessible(accessible);
        return ret;
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
        if(Object.class.equals(clazz)) {
            log.error(": no field found:[{}] !", fieldName);
            return null;
        }
        try {
            clazz = clazz.getSuperclass();
            Field field = clazz.getDeclaredField(fieldName);
            return !containPrivate && Modifier.isPrivate(field.getModifiers()) ? null : field;
        } catch(NoSuchFieldException e) {
            return getSuperField(clazz, fieldName, containPrivate);
        }
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

    public static boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    public static void saveProxyClass(String savePath, Object object, Object proxy) throws Exception {
        Class<?> clazz = Class.forName("sun.misc.ProxyGenerator");
        Method method = clazz.getMethod("generateProxyClass", String.class, Class[].class);
        byte[] b = (byte[]) method.invoke(null, proxy.getClass().getName(), new Class[] {object.getClass()});
        FileOutputStream out = new FileOutputStream(new File(savePath + proxy.getClass().getName() + ".class"));
        out.write(b);
        out.flush();
        out.close();
    }

    /**
     * 根据属性参数，解析出 object 中对应的属性值
     * @param param     属性参数，eg: obj.value
     * @param obj       包含 obj 属性的对象
     * @return          属性值
     * @throws Exception
     */
    public static Object parseValue(String param, Object obj) throws Exception {
        String[] fields = param.split("\\.");
        for (String field : fields) {
            obj = getFieldValue(obj, field);
        }
        return obj;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 parseValue() 过程相反
     * @param param     属性参数 eg: obj.field
     * @param obj       包含 obj 属性的对象
     * @param value     属性对象 obj 中的 field 属性的值
     * @throws Exception
     */
    public static void parseField(String param, Object obj, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for(int i = 0; i < fields.length; i++) {
            Field field = getField(clazz, fields[i]);
            if(i == fields.length - 1) {
                setFieldValue(obj, field, value);
                break;
            }
            if(field.get(obj) == null) {
                setFieldValue(obj, field, (obj = newInstance(field.getType())));
            }
            clazz = field.getType();
        }
    }
}
