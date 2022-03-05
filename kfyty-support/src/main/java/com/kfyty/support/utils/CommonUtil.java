package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

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
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

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
     * #{}、${} 正则匹配
     */
    public static final Pattern PARAMETERS_PATTERN = Pattern.compile("(\\$\\{.*?})|(#\\{.*?})");

    /**
     * 空对象数组
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 空 Class 数组
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    public static boolean empty(Object obj) {
        if (obj != null && CharSequence.class.isAssignableFrom(obj.getClass())) {
            return ((CharSequence) obj).toString().trim().length() < 1;
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
        return 1;
    }

    public static String getGetter(String name) {
        return "get" + (name.length() == 1 ? name.toUpperCase() : Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static String getSetter(String name) {
        return "set" + (name.length() == 1 ? name.toUpperCase() : Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static void consumer(Object o, Consumer<Object> consumer) {
        consumer(o, consumer, entry -> entry);
    }

    public static void consumer(Object o, Consumer<Object> consumer, Function<Map.Entry<?, ?>, Object> entryMapping) {
        mapping(o, e -> {
            consumer.accept(e);
            return null;
        }, entryMapping);
    }

    public static <T> List<T> mapping(Object o, Function<Object, T> mapping) {
        return mapping(o, mapping, entry -> entry);
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

    public static List<String> split(String source, String pattern) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toList());
    }

    public static Set<String> split(String source, String pattern, boolean distinct) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toSet());
    }

    public static List<Object> toList(Object value) {
        return toList(value, entry -> entry);
    }

    public static List<Object> toList(Object value, Function<Map.Entry<?, ?>, Object> entryMapping) {
        if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        }
        if (value.getClass().isArray()) {
            return Stream.iterate(0, i -> i + 1).limit(size(value)).map(i -> Array.get(value, i)).collect(Collectors.toList());
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).entrySet().stream().map(entryMapping).collect(Collectors.toList());
        }
        log.error("data to list error, data is not collection, array or map !");
        return emptyList();
    }

    public static String underline2CamelCase(String s) {
        return underline2CamelCase(s, false);
    }

    public static String underline2CamelCase(String target, boolean isClass) {
        if (empty(target)) {
            throw new SupportException("convert underline to camel case failed, target can't empty !");
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
            throw new SupportException("convert camel case to underline failed, target can't empty !");
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

    public static String format(String s, Object... params) {
        int index = -1;
        int paramIndex = 0;
        StringBuilder sb = new StringBuilder(s);
        while ((index = sb.indexOf("{}", index)) != -1) {
            sb.replace(index, index + 2, ofNullable(params[paramIndex++]).map(Object::toString).orElse(""));
        }
        return sb.toString();
    }

    public static String processPlaceholder(String s, Map<String, Object> params) {
        Matcher matcher = SIMPLE_PARAMETERS_PATTERN.matcher(s);
        while (matcher.find()) {
            String key = matcher.group().replaceAll("[{}]", "");
            s = s.replace(matcher.group(), String.valueOf(params.get(key)));
        }
        return s;
    }

    public static String formatURI(String uri) {
        uri = uri.trim();
        uri = uri.startsWith("/") ? uri : "/" + uri;
        return !uri.endsWith("/") ? uri : uri.substring(0, uri.length() - 1);
    }

    public static String removePrefix(String prefix, String target) {
        if (target.startsWith(prefix)) {
            return target.replaceFirst(prefix, "");
        }
        return target;
    }

    public static String resolveAttribute(Element element, String name) {
        return resolveAttribute(element, name, null);
    }

    public static String resolveAttribute(Element element, String name, Supplier<RuntimeException> emptyException) {
        String attribute = element.getAttribute(name);
        if (emptyException != null && CommonUtil.empty(attribute)) {
            throw emptyException.get();
        }
        return attribute;
    }

    public static void ensureFolderExists(String path) {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new SupportException("ensure folder exists failed !");
        }
    }

    public static List<File> scanFiles(String path) {
        return scanFiles(path, e -> true);
    }

    public static List<File> scanFiles(String path, Predicate<File> filePredicate) {
        return scanFiles(path, filePredicate, Thread.currentThread().getContextClassLoader());
    }

    public static List<File> scanFiles(String path, Predicate<File> filePredicate, ClassLoader classLoader) {
        try {
            URL root = Objects.requireNonNull(classLoader.getResource(""));
            File file = new File(root.getPath() + path);
            if (file.isFile()) {
                return filePredicate.test(file) ? singletonList(file) : emptyList();
            }
            File[] files = file.listFiles();
            return files == null ? emptyList() : Arrays.stream(files).filter(File::isFile).filter(filePredicate).collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

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

    public static void close(Object obj) {
        if (!(obj instanceof AutoCloseable)) {
            throw new SupportException("can't close !");
        }
        try {
            ((AutoCloseable) obj).close();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
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
            ensureFolderExists(savePath);
            FileOutputStream out = new FileOutputStream(savePath + proxy.getClass().getName() + ".class");
            out.write(b);
            out.flush();
            close(out);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
