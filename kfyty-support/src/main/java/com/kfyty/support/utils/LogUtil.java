package com.kfyty.support.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * 描述: 打印日志工具
 *
 * @author kfyty725
 * @date 2021/6/9 17:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class LogUtil {

    public static void logIfDebugEnabled(BiConsumer<Logger, Object[]> print, Object ... params) {
        if(log.isDebugEnabled()) {
            print.accept(log, params);
        }
    }

    public static <T> T logIfDebugEnabled(T defaultValue, BiConsumer<Logger, Object[]> print, Object ... params) {
        logIfDebugEnabled(print, params);
        return defaultValue;
    }

    public static void logIfWarnEnabled(BiConsumer<Logger, Object[]> print, Object ... params) {
        if(log.isWarnEnabled()) {
            print.accept(log, params);
        }
    }

    public static <T> T logIfWarnEnabled(T defaultValue, BiConsumer<Logger, Object[]> print, Object ... params) {
        logIfWarnEnabled(print, params);
        return defaultValue;
    }
}
