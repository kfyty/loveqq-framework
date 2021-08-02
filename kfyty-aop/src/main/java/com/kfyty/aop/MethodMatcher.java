package com.kfyty.aop;

import java.lang.reflect.Method;

/**
 * 描述: 方法匹配
 *
 * @author kfyty725
 * @date 2021/7/29 11:21
 * @email kfyty725@hotmail.com
 */
public interface MethodMatcher {
    boolean matches(Method method, Class<?> targetClass);
}
