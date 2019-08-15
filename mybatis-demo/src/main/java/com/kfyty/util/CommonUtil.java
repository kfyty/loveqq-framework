package com.kfyty.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 功能描述: 通用工具类
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/27 11:07
 * @since JDK 1.8
 */
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
            if(index < s.length() - 1) {
                char ch = s.charAt(index + 1);
                s = s.replace("_" + ch, "" + Character.toUpperCase(ch));
            }
        }
        return !isClass ? s : s.length() == 1 ? Character.toUpperCase(s.charAt(0)) + "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String getBinaryBit(int n) {
        int bit = Integer.SIZE;
        StringBuilder result = new StringBuilder(bit);
        for(int i = 0; i < bit; i++) {
            result.append(n << i >> bit - 1 == -1 ? '1' : '0');
        }
        return result.toString();
    }

    public static String getBinaryBit(long n) {
        int bit = Long.SIZE;
        StringBuilder result = new StringBuilder(bit);
        for(int i = 0; i < bit; i++) {
            result.append(n << i >> bit - 1 == -1 ? '1' : '0');
        }
        return result.toString();
    }
}
