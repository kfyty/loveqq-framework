package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
public abstract class CommonUtil {
    /**
     * 全部大写字母或数字的正则表达式
     */
    public static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z0-9]*");

    public static boolean empty(Object obj) {
        if(obj != null && CharSequence.class.isAssignableFrom(obj.getClass())) {
            return ((CharSequence) obj).toString().trim().length() < 1;
        }
        return size(obj) < 1;
    }

    public static boolean notEmpty(Object obj) {
        return !empty(obj);
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

    public static List<String> split(String source, String pattern) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toList());
    }

    public static Set<String> split(String source, String pattern, boolean distinct) {
        return Arrays.stream(source.split(pattern)).filter(CommonUtil::notEmpty).collect(Collectors.toSet());
    }

    public static List<Object> convert2List(Object value) {
        List<Object> list = new ArrayList<>();
        if(value instanceof Collection) {
            list.addAll((Collection<?>) value);
        } else if(value.getClass().isArray()) {
            list.addAll(Arrays.asList((Object[]) value));
        } else {
            log.error("convert data error, parameter is not collection or array !");
            return Collections.emptyList();
        }
        return list;
    }

    public static String getStackTrace(Throwable throwable) {
        if(throwable == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter, true));
        return stringWriter.toString();
    }

    public static void close(Object obj) {
        if(obj == null) {
            return;
        }
        if(!(obj instanceof AutoCloseable)) {
            throw new SupportException("can't close !");
        }
        try {
            ((AutoCloseable) obj).close();
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }

    public static String convert2Hump(String s) {
        return convert2Hump(s, false);
    }

    public static String convert2Hump(String target, boolean isClass) {
        if(empty(target)) {
            throw new SupportException("convert to hump failed, target can't empty !");
        }
        int index = 0;
        target = UPPER_CASE_PATTERN.matcher(target).matches() || target.contains("_") ? target.toLowerCase() : target;
        while(target.contains("_")) {
            index = target.indexOf('_', index);
            if(index == target.length() - 1) {
                break;
            }
            char ch = target.charAt(index + 1);
            target = target.replace("_" + ch, "" + Character.toUpperCase(ch));
        }
        return !isClass ? target : target.length() == 1 ? target.toUpperCase() : Character.toUpperCase(target.charAt(0)) + target.substring(1);
    }

    public static String convert2Underline(String s) {
        return convert2Underline(s, true);
    }

    public static String convert2Underline(String target, boolean lower) {
        if(empty(target)) {
            throw new SupportException("convert to underline failed, target can't empty !");
        }
        if(UPPER_CASE_PATTERN.matcher(target).matches()) {
            return lower ? target.toLowerCase() : target.toUpperCase();
        }
        char c = target.charAt(0);
        StringBuilder builder = new StringBuilder();
        builder.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : c);
        for(int i = 1; i < target.length(); i++) {
            c = target.charAt(i);
            if(Character.isUpperCase(c)) {
                builder.append("_").append(Character.toLowerCase(c));
                continue;
            }
            builder.append(c);
        }
        return lower ? builder.toString() : builder.toString().toUpperCase();
    }

    public static String format(String s, Object ... params) {
        int index = -1;
        int paramIndex = 0;
        StringBuilder sb = new StringBuilder(s);
        while((index = sb.indexOf("{}", index)) != -1) {
            sb.replace(index, index + 2, Optional.ofNullable(params[paramIndex++]).map(Object::toString).orElse(""));
        }
        return sb.toString();
    }

    public static String formatURI(String uri) {
        uri = uri.trim();
        uri = uri.startsWith("/") ? uri : "/" + uri;
        return !uri.endsWith("/") ? uri : uri.substring(0, uri.length() - 1);
    }

    public static void saveJdkProxyClass(String savePath, Object object, Object proxy) {
        try {
            Class<?> clazz = Class.forName("sun.misc.ProxyGenerator");
            Method method = clazz.getMethod("generateProxyClass", String.class, Class[].class);
            byte[] b = (byte[]) method.invoke(null, proxy.getClass().getName(), new Class[] {object.getClass()});
            FileOutputStream out = new FileOutputStream(savePath + proxy.getClass().getName() + ".class");
            out.write(b);
            out.flush();
            close(out);
        } catch (Exception e) {
            throw new SupportException(e);
        }
    }
}
