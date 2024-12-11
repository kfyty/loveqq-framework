package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kfyty.loveqq.framework.core.utils.StreamUtil.throwMergeFunction;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * 功能描述: 通用工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/6/27 11:07
 * @since JDK 1.8
 */
@Slf4j
public abstract class CommonUtil {
    /**
     * 是否支持虚拟线程
     */
    public static boolean VIRTUAL_THREAD_SUPPORTED = false;

    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

    /**
     * CPU 核心数
     */
    public static final int CPU_CORE = Runtime.getRuntime().availableProcessors();

    /**
     * 空行的正则表达式
     */
    public static final Pattern BLANK_LINE_PATTERN = Pattern.compile("(?m)^\\s*$" + System.lineSeparator());

    /**
     * 可以匹配任何内容的正则表达式
     */
    public static final Pattern MATCH_ALL_PATTERN = Pattern.compile("([\\s\\S]*)");

    /**
     * 全部大写字母或数字的正则表达式
     */
    public static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z0-9]*");

    /**
     * {} 正则匹配
     */
    public static final Pattern SIMPLE_PARAMETERS_PATTERN = Pattern.compile("(\\{.*?})");

    /**
     * ${} 正则匹配
     */
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.*?})");

    /**
     * #{}、${} 正则匹配
     */
    public static final Pattern PARAMETERS_PATTERN = Pattern.compile("(\\$\\{.*?})|(#\\{.*?})");

    /**
     * 空字节数组
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * 空输入流
     */
    public static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(EMPTY_BYTE_ARRAY);

    /**
     * 空对象数组
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 空字符串数组
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 空注解数组
     */
    public static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    /**
     * 空 Class 数组
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    static {
        try {
            Class.forName("java.lang.BaseVirtualThread", false, CommonUtil.class.getClassLoader());
            VIRTUAL_THREAD_SUPPORTED = true;
        } catch (Throwable e) {
            // ignored
        }
    }

    /* ------------------------------------------ 集合操作 ------------------------------------------ */

    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static boolean empty(Object obj) {
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() < 1;
        }
        return size(obj) < 1;
    }

    public static boolean notEmpty(Object obj) {
        return !empty(obj);
    }

    public static int size(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }
        if (obj instanceof Optional<?>) {
            return ((Optional<?>) obj).isPresent() ? 1 : 0;
        }
        return 1;
    }

    public static List<?> toList(Object value) {
        return toList(value, Map.Entry::getValue);
    }

    public static List<?> toList(Object value, Function<Map.Entry<?, ?>, ?> entryMapping) {
        if (value instanceof List) {
            return (List<?>) value;
        }
        if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        }
        if (value.getClass().isArray()) {
            return Stream.iterate(0, i -> i + 1).limit(size(value)).map(i -> Array.get(value, i)).collect(Collectors.toList());
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).entrySet().stream().map(entryMapping).collect(Collectors.toList());
        }
        return Collections.singletonList(value);
    }

    public static Object copyToArray(Class<?> elementType, Collection<?> collection) {
        int index = 0;
        Object instance = Array.newInstance(elementType, collection.size());
        for (Object o : collection) {
            Array.set(instance, index++, o);
        }
        return instance;
    }

    /* ------------------------------------------ 字符串操作 ------------------------------------------ */

    public static String getGetter(String name) {
        return "get" + (name.length() == 1 ? name.toUpperCase() : Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static String getSetter(String name) {
        return "set" + (name.length() == 1 ? name.toUpperCase() : Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static List<String> split(String source, String pattern) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toList());
    }

    public static <T> List<T> split(String source, String pattern, Function<String, T> mapping) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).map(mapping).collect(Collectors.toList());
    }

    public static Set<String> split(String source, String pattern, boolean distinct) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toSet());
    }

    public static <T> Set<T> split(String source, String pattern, Function<String, T> mapping, boolean distinct) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).map(mapping).collect(Collectors.toSet());
    }

    public static void split(String source, String start, String end, Consumer<String> substring) {
        int startIndex = source.indexOf(start);
        int endIndex = source.indexOf(end, startIndex);
        while (startIndex != -1 && endIndex != -1) {
            substring.accept(source.substring(startIndex + start.length(), endIndex));
            startIndex = source.indexOf(start, endIndex);
            endIndex = source.indexOf(end, startIndex);
        }
    }

    public static String format(String s, Object... params) {
        int index = -1;
        int preIndex = 0;
        int paramIndex = 0;
        char[] charArray = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        while ((index = s.indexOf("{}", index + 1)) != -1) {
            sb.append(charArray, preIndex, index - preIndex);
            sb.append(ofNullable(params[paramIndex++]).map(Object::toString).orElse(EMPTY_STRING));
            preIndex = index + 2;
        }
        sb.append(charArray, preIndex, charArray.length - preIndex);
        return sb.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String processPlaceholder(String s, Map<String, String> params) {
        return PlaceholdersUtil.resolve(s, EMPTY_STRING, "{", "}", (Map) params);
    }

    public static String removePrefix(String prefix, String target) {
        if (target.startsWith(prefix)) {
            return target.substring(prefix.length());
        }
        return target;
    }

    public static String toString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value.getClass().isArray()) {
            return toList(value).toString();
        }
        return value.toString();
    }

    /* ------------------------------------------ URI 字符串操作 ------------------------------------------ */

    public static String formatURI(String uri) {
        uri = uri.trim();
        if (uri.isEmpty()) {
            return uri;
        }
        uri = uri.charAt(0) == '/' ? uri : '/' + uri;
        return uri.length() == 1 ? uri : uri.charAt(uri.length() - 1) == '/' ? uri.substring(0, uri.length() - 1) : uri;
    }

    public static Map<String, String> resolveURLParameters(String url) {
        return resolveURLParameters(url, EMPTY_STRING);
    }

    public static Map<String, String> resolveURLParameters(String url, String prefix) {
        if (empty(url) || url.indexOf('=') < 0) {
            return Collections.emptyMap();
        }
        int index = url.indexOf('?');
        String parameter = index < 0 ? url : url.substring(index + 1);
        String paramPrefix = empty(prefix) ? EMPTY_STRING : prefix + '.';
        Map<String, String> query = new HashMap<>();
        List<String> split = split(parameter, "&");
        for (String params : split) {
            String[] paramPair = params.split("=");
            if (paramPair.length > 1) {
                query.put(paramPrefix + paramPair[0], paramPair[1]);
            }
        }
        return query;
    }

    /* ------------------------------------------ 驼峰/下划线转换操作 ------------------------------------------ */

    public static String underline2CamelCase(String s) {
        return underline2CamelCase(s, false);
    }

    public static String underline2CamelCase(String target, boolean isClass) {
        if (empty(target)) {
            throw new ResolvableException("convert underline to camel case failed, target can't empty !");
        }
        StringBuilder builder = new StringBuilder();
        target = UPPER_CASE_PATTERN.matcher(target).matches() || target.contains("_") ? target.toLowerCase() : target;
        for (int i = 0; i < target.length(); i++) {
            char c = target.charAt(i);
            if (c != '_') {
                builder.append(c);
                continue;
            }
            while (i < target.length() && (c = target.charAt(i)) == '_') {
                i++;
            }
            if (i < target.length()) {
                builder.append(i == 1 ? c : Character.toUpperCase(c));
            }
        }
        target = builder.toString();
        return !isClass ? target : target.length() == 1 ? target.toUpperCase() : Character.toUpperCase(target.charAt(0)) + target.substring(1);
    }

    public static String camelCase2Underline(String s) {
        return camelCase2Underline(s, true);
    }

    public static String camelCase2Underline(String target, boolean lower) {
        if (empty(target)) {
            throw new ResolvableException("convert camel case to underline failed, target can't empty !");
        }
        if (UPPER_CASE_PATTERN.matcher(target).matches()) {
            return lower ? target.toLowerCase() : target.toUpperCase();
        }
        char c = target.charAt(0);
        StringBuilder builder = new StringBuilder();
        builder.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : c);
        for (int i = 1; i < target.length(); i++) {
            c = target.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append('_').append(Character.toLowerCase(c));
                continue;
            }
            builder.append(c);
        }
        return lower ? builder.toString() : builder.toString().toUpperCase();
    }

    /* ------------------------------------------ 集合排序操作 ------------------------------------------ */

    public static <K, V> Map<K, V> sort(Map<K, V> unsortedMap, Comparator<Map.Entry<K, V>> comparator) {
        Supplier<Map<K, V>> mapFactory = () -> new LinkedHashMap<>((int) (unsortedMap.size() / .75F) + 1);
        return unsortedMap
                .entrySet()
                .stream()
                .sorted(comparator)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, throwMergeFunction(), mapFactory));
    }

    /* ------------------------------------------ 集合消费/映射操作 ------------------------------------------ */

    public static void consumer(Object o, Consumer<Object> consumer) {
        consumer(o, consumer, Map.Entry::getValue);
    }

    public static void consumer(Object o, Consumer<Object> consumer, Function<Map.Entry<?, ?>, Object> entryMapping) {
        mapping(o, e -> {
            consumer.accept(e);
            return null;
        }, entryMapping);
    }

    public static <T> List<T> mapping(Object o, Function<Object, T> mapping) {
        return mapping(o, mapping, Map.Entry::getValue);
    }

    public static <T> List<T> mapping(Object o, Function<Object, T> mapping, Function<Map.Entry<?, ?>, Object> entryMapping) {
        if (o == null) {
            return emptyList();
        }
        if (o instanceof Collection) {
            return ((Collection<?>) o).stream().map(mapping).collect(Collectors.toList());
        }
        if (o.getClass().isArray()) {
            return (o instanceof Object[] ? Arrays.stream((Object[]) o) : toList(o).stream()).map(mapping).collect(Collectors.toList());
        }
        if (o instanceof Map) {
            return ((Map<?, ?>) o).entrySet().stream().map(entryMapping).map(mapping).collect(Collectors.toList());
        }
        return singletonList(mapping.apply(o));
    }

    /* ------------------------------------------ 其他操作 ------------------------------------------ */

    public static void sleep(long time) {
        sleep(time, TimeUnit.MILLISECONDS);
    }

    public static void sleep(long time, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Map<String, String> loadCommandLineProperties(String[] commandLineArgs, String keySeparator) {
        Map<String, String> propertySources = new HashMap<>(8);
        for (String key : commandLineArgs) {
            if (key.startsWith(keySeparator)) {
                int index = key.indexOf('=');
                if (index == -1) {
                    throw new IllegalArgumentException("Please set property value of key: " + key);
                }
                propertySources.put(key.substring(keySeparator.length(), index), key.substring(index + 1));
            }
        }
        return propertySources;
    }

    public static int hashCode(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof char[]) {
            return Arrays.hashCode((char[]) o);
        }
        if (o instanceof byte[]) {
            return Arrays.hashCode((byte[]) o);
        }
        if (o instanceof short[]) {
            return Arrays.hashCode((short[]) o);
        }
        if (o instanceof int[]) {
            return Arrays.hashCode((int[]) o);
        }
        if (o instanceof long[]) {
            return Arrays.hashCode((long[]) o);
        }
        if (o instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) o);
        }
        if (o instanceof float[]) {
            return Arrays.hashCode((float[]) o);
        }
        if (o instanceof double[]) {
            return Arrays.hashCode((double[]) o);
        }
        if (o instanceof Object[]) {
            return Arrays.deepHashCode((Object[]) o);
        }
        return o.hashCode();
    }

    /**
     * 保存 jdk 生成的代理类
     *
     * @param savePath 保存路径
     * @param proxy    代理对象
     */
    public static void saveJdkProxyClass(String savePath, Object proxy) {
        try {
            Class<?> clazz = ReflectUtil.load("sun.misc.ProxyGenerator");
            Method method = ReflectUtil.getMethod(clazz, "generateProxyClass", String.class, Class[].class);
            byte[] b = (byte[]) ReflectUtil.invokeMethod(null, method, proxy.getClass().getName(), proxy.getClass().getInterfaces());
            IOUtil.ensureFolderExists(savePath);
            IOUtil.close(IOUtil.write(new FileOutputStream(savePath + proxy.getClass().getName() + ".class"), b));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
