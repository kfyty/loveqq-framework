package com.kfyty.boot.validator.context;

import com.kfyty.boot.validator.support.IOC;

import java.util.Objects;

/**
 * 描述: ioc 上下文
 *
 * @author kfyty725
 * @date 2023/4/17 14:28
 * @email kfyty725@hotmail.com
 */
public abstract class IOCContext {
    private static IOC IOC;

    public static void setIOC(IOC ioc) {
        IOCContext.IOC = ioc;
    }

    public static <T> T getBean(Class<T> clazz) {
        return Objects.requireNonNull(IOCContext.IOC, "IOC required").getBean(clazz);
    }
}
