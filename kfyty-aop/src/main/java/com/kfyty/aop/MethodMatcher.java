package com.kfyty.aop;

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

    ShadowMatch getShadowMatch(Method method);

    boolean matches(Method method, Class<?> targetClass);
}
