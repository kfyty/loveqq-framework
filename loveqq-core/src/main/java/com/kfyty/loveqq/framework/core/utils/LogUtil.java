package com.kfyty.loveqq.framework.core.utils;

import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * 描述: 打印日志工具
 *
 * @author kfyty725
 * @date 2021/6/9 17:05
 * @email kfyty725@hotmail.com
 */
public abstract class LogUtil {

    public static void logIfDebugEnabled(Logger log, Consumer<Logger> loggerConsumer) {
        if (log.isDebugEnabled()) {
            loggerConsumer.accept(log);
        }
    }

    public static <T> T logIfDebugEnabled(Logger log, Consumer<Logger> loggerConsumer, T defaultValue) {
        logIfDebugEnabled(log, loggerConsumer);
        return defaultValue;
    }

    public static void logIfWarnEnabled(Logger log, Consumer<Logger> loggerConsumer) {
        if (log.isWarnEnabled()) {
            loggerConsumer.accept(log);
        }
    }

    public static <T> T logIfWarnEnabled(Logger log, Consumer<Logger> loggerConsumer, T defaultValue) {
        logIfWarnEnabled(log, loggerConsumer);
        return defaultValue;
    }
}
