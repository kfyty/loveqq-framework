package com.kfyty.loveqq.framework.aop;

import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 描述: 方法匹配
 *
 * @author kfyty725
 * @date 2021/7/29 11:21
 * @email kfyty725@hotmail.com
 */
public interface MethodMatcher {
    /**
     * {@link ShadowMatch}
     *
     * @param method 要匹配的方法
     * @return {@link ShadowMatch}
     */
    ShadowMatch getShadowMatch(Method method);

    /**
     * 方法是否匹配
     *
     * @param method      要匹配的方法
     * @param targetClass 方法所在的类
     * @return true if match
     */
    boolean matches(Method method, Class<?> targetClass);
}
