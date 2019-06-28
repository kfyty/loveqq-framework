package com.kfyty.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * 功能描述: 通用工具类
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/27 11:07
 * @since JDK 1.8
 */
public class CommonUtil {
    public static String getStackTrace(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(throwable1 -> {
                    StringWriter stringWriter = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(stringWriter, true));
                    return stringWriter.toString();
                }).orElseThrow(() ->new NullPointerException("throwable is null"));
    }
}
