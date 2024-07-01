package com.kfyty.loveqq.framework.core.proxy.aop;

import org.aspectj.lang.JoinPoint;

/**
 * 描述: 暴露 MethodInvocationProceedingJoinPoint
 *
 * @author kfyty725
 * @date 2021/8/1 14:33
 * @email kfyty725@hotmail.com
 */
public abstract class JoinPointHolder {
    private static final ThreadLocal<JoinPoint> CURRENT_JOIN_POINT = new ThreadLocal<>();

    /**
     * 设置 {@link JoinPoint}
     *
     * @param joinPoint {@link JoinPoint}
     * @return 当前线程之前的 {@link JoinPoint}
     */
    public static JoinPoint set(JoinPoint joinPoint) {
        JoinPoint prev = CURRENT_JOIN_POINT.get();
        CURRENT_JOIN_POINT.set(joinPoint);
        return prev;
    }

    /**
     * 获取 {@link JoinPoint}
     *
     * @return {@link JoinPoint}
     */
    public static JoinPoint currentJoinPoint() throws IllegalStateException {
        JoinPoint joinPoint = CURRENT_JOIN_POINT.get();
        if (joinPoint == null) {
            throw new IllegalStateException("The join point doesn't exists on this thread !");
        }
        return joinPoint;
    }
}
