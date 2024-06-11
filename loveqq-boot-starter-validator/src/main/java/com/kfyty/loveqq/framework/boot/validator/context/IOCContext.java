package com.kfyty.loveqq.framework.boot.validator.context;

import java.util.Objects;
import java.util.function.Function;

/**
 * 描述: ioc 上下文
 *
 * @author kfyty725
 * @date 2023/4/17 14:28
 * @email kfyty725@hotmail.com
 */
public abstract class IOCContext {
    private static Function<Class<?>, Object> IOC;

    public static void setIOC(Function<Class<?>, Object> ioc) {
        IOCContext.IOC = ioc;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return (T) Objects.requireNonNull(IOCContext.IOC, "IOC required").apply(clazz);
    }
}
