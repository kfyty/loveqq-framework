package com.kfyty.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static boolean empty(String s) {
        return !Optional.ofNullable(s).filter(e -> e.trim().length() != 0).isPresent();
    }

    public static <T> boolean empty(T[] t) {
        return !Optional.ofNullable(t).filter(e -> e.length != 0).isPresent();
    }

    public static boolean empty(Collection c) {
        return !Optional.ofNullable(c).filter(e -> !e.isEmpty()).isPresent();
    }

    public static boolean empty(Map m) {
        return !Optional.ofNullable(m).filter(e -> !e.isEmpty()).isPresent();
    }

    public static String getStackTrace(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(throwable1 -> {
                    StringWriter stringWriter = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(stringWriter, true));
                    return stringWriter.toString();
                }).orElseThrow(() -> new NullPointerException("throwable is null"));
    }

    public static String convert2Hump(String s, boolean isClass) {
        s = Optional.ofNullable(s).map(e -> e.contains("_") || Pattern.compile("[A-Z0-9]*").matcher(e).matches() ? e.toLowerCase() : e).orElseThrow(() -> new NullPointerException("column is null"));
        while(s.contains("_")) {
            int index = s.indexOf('_');
            if(index == s.length() - 1) {
                break;
            }
            char ch = s.charAt(index + 1);
            s = s.replace("_" + ch, "" + Character.toUpperCase(ch));
        }
        return !isClass ? s : s.length() == 1 ? Character.toUpperCase(s.charAt(0)) + "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String convert2JavaType(String dataBaseType) {
        switch (dataBaseType.toLowerCase()) {
            case "char":
            case "text":
            case "varchar":
            case "varchar2":
            case "nvarchar2" :
                return "String";
            case "number":
            case "bigint":
                return "Long";
            case "tinyint":
            case "int":
            case "integer":
                return "Integer";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "decimal" :
                return "BigDecimal";
            case "time":
            case "date":
            case "datetime":
            case "datetime2":
            case "timestamp" :
                return "Date";
            case "blob" :
            case "longblob" :
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
            if(sb.charAt(index - 1) == '#' || sb.charAt(index - 1) == '$') {
                index++;
                continue;
            }
            sb.replace(index, index + 2, Optional.ofNullable(params[paramIndex++]).map(Object::toString).orElse(""));
        }
        return sb.toString();
    }

    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) throws Exception {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        Field field = invocationHandler.getClass().getDeclaredField("memberValues");
        field.setAccessible(true);
        Map memberValues = (Map) field.get(invocationHandler);
        memberValues.put(annotationField, value);
    }

    public static List<Object> convert2List(Object value) {
        List<Object> list = new ArrayList<>();
        if(value instanceof Collection) {
            list.addAll((Collection) value);
        } else if(value.getClass().isArray()) {
            list.addAll(Arrays.asList((Object[]) value));
        } else {
            log.error(": convert data error, parameter is not collection or array !");
            return null;
        }
        return list;
    }

    public static <T> Class<T> convert2Wrapper(Class<T> clazz) throws Exception {
        String type = clazz.getSimpleName();
        return Character.isUpperCase(type.charAt(0)) ? clazz :
                type.equals("int") ? (Class<T>) Integer.class : (Class<T>) Class.forName("java.lang." + Character.toUpperCase(type.charAt(0)) + type.substring(1));
    }

    public static boolean isBaseDataType(Class clazz) {
        return byte.class.isAssignableFrom(clazz)           ||
                short.class.isAssignableFrom(clazz)         ||
                int.class.isAssignableFrom(clazz)           ||
                long.class.isAssignableFrom(clazz)          ||
                float.class.isAssignableFrom(clazz)         ||
                double.class.isAssignableFrom(clazz)        ||
                Number.class.isAssignableFrom(clazz)        ||
                BigInteger.class.isAssignableFrom(clazz)    ||
                BigDecimal.class.isAssignableFrom(clazz)    ||
                CharSequence.class.isAssignableFrom(clazz)  ||
                Date.class.isAssignableFrom(clazz);
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
        if(clazz.getSimpleName().equals("Object")) {
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
        if(clazz == null || clazz.getSimpleName().equals("Object")) {
            return new HashMap<>(0);
        }
        clazz = clazz.getSuperclass();
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
        for(int i = 0; i < fields.length; i++) {
            Field field = obj.getClass().getDeclaredField(fields[i]);
            field.setAccessible(true);
            obj = field.get(obj);
        }
        return obj;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 @see parseValue() 过程相反
     * @param param     属性参数 eg: obj.field
     * @param obj       包含 obj 属性的对象
     * @param value     属性对象 obj 中的 field 属性的值
     * @throws Exception
     */
    public static void parseField(String param, Object obj, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for(int i = 0; i < fields.length; i++) {
            Field field = clazz.getDeclaredField(fields[i]);
            field.setAccessible(true);
            if(i == fields.length - 1) {
                field.set(obj, value);
                break;
            }
            if(field.get(obj) == null) {
                Object fieldValue = field.getType().newInstance();
                field.set(obj, fieldValue);
                obj = fieldValue;
            }
            clazz = field.getType();
        }
    }
}
