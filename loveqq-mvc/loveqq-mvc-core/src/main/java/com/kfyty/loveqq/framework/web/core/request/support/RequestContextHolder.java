package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder {
    /**
     * 请求 trace id 属性
     * 响应式请求有值
     */
    public static final String REQUEST_TRACE_ID_ATTRIBUTE = RequestContextHolder.class.getName() + ".REQUEST_TRACE_ID_ATTRIBUTE";

    /**
     * 线程本地缓存
     */
    private static final ThreadLocal<ServerRequest> REQUEST_LOCAL = new ThreadLocal<>();

    public static ServerRequest get() {
        ServerRequest request = REQUEST_LOCAL.get();
        if (request == null) {
            throw new IllegalStateException("The current thread is not bind to request !");
        }
        return request;
    }

    public static ServerRequest set(ServerRequest request) {
        ServerRequest prev = REQUEST_LOCAL.get();
        REQUEST_LOCAL.set(request);
        return prev;
    }

    /**
     * 设置线程上下文 trace id 并执行，主要是支持响应式
     *
     * @param request  请求
     * @param supplier 处理器
     * @return 返回值
     */
    public static <T> T callWithTraceId(ServerRequest request, Supplier<T> supplier) {
        String prev = MDC.get(ConstantConfig.TRACE_ID);
        try {
            MDC.put(ConstantConfig.TRACE_ID, (String) request.getAttribute(REQUEST_TRACE_ID_ATTRIBUTE));
            return supplier.get();
        } finally {
            if (prev == null) {
                MDC.remove(ConstantConfig.TRACE_ID);
            } else {
                MDC.put(ConstantConfig.TRACE_ID, prev);
            }
        }
    }

    /**
     * 设置线程上下文 trace id 并执行，主要是支持响应式
     *
     * @param request  请求
     * @param runnable 处理器
     */
    public static void runWithTraceId(ServerRequest request, Runnable runnable) {
        String prev = MDC.get(ConstantConfig.TRACE_ID);
        try {
            MDC.put(ConstantConfig.TRACE_ID, (String) request.getAttribute(REQUEST_TRACE_ID_ATTRIBUTE));
            runnable.run();
        } finally {
            if (prev == null) {
                MDC.remove(ConstantConfig.TRACE_ID);
            } else {
                MDC.put(ConstantConfig.TRACE_ID, prev);
            }
        }
    }
}
