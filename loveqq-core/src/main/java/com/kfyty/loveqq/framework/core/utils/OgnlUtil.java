package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import ognl.Ognl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: ognl 工具
 *
 * @author kfyty725
 * @date 2022/7/20 15:31
 * @email kfyty725@hotmail.com
 */
public abstract class OgnlUtil {
    /**
     * 解析表达式并返回 boolean 值
     *
     * @param express 表达式
     * @param root    根对象
     * @return true/false
     */
    public static boolean getBoolean(String express, Object root) {
        Boolean value = compute(express, root, Boolean.class);
        return value != null && value;
    }

    /**
     * 解析表达式
     *
     * @param express 表达式
     * @param root    跟对象
     * @return 值
     */
    public static String compute(String express, Object root) {
        return compute(express, root, String.class);
    }

    /**
     * 解析表达式
     *
     * @param express 表达式
     * @param root    跟对象
     * @param clazz   返回值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T compute(String express, Object root, Class<T> clazz) {
        try {
            return (T) Ognl.getValue(express, root, clazz);
        } catch (Throwable e) {
            throw new ResolvableException("ognl express compute failed: " + express + " -> " + e.getMessage(), e);
        }
    }

    /**
     * 基于 ${} 占位符解析 ognl 表达式，仅有占位符内的会使用 ognl 计算
     *
     * @param express 占位符表达式
     * @param context 上下文
     * @return 计算结果
     */
    public static String computePlaceholders(String express, Object context) {
        return computePlaceholders(express, context, null);
    }

    /**
     * 基于 ${} 占位符解析 ognl 表达式，仅有占位符内的会使用 ognl 计算
     *
     * @param express         占位符表达式
     * @param context         上下文
     * @param propertyContext 配置属性上下文，优先从配置里获取，否则从 ognl 计算上下文获取
     * @return 计算结果
     */
    public static String computePlaceholders(String express, Object context, PropertyContext propertyContext) {
        int index = -1;
        int length = express.length();
        StringBuilder builder = new StringBuilder();
        while (++index < length) {
            char c = express.charAt(index);
            if (c == '$' && index != length - 1 && express.charAt(index + 1) == '{') {
                int endIndex = express.indexOf('}', index + 2);
                String variable = express.substring(index + 2, endIndex);
                String property = propertyContext == null ? null : propertyContext.getProperty(variable);
                if (property != null) {
                    builder.append(property);
                } else {
                    String computed = compute(variable, context);
                    builder.append(computed);
                }
                index = endIndex;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * 构建上下文
     * 其中 $$ 表示 {@link BeanFactory}，可使用 $$.getBean(name) 获取 bean
     *
     * @param target 当前实例
     * @param method 当前执行方法
     * @param args   方法参数
     * @return 上下文
     */
    public static Map<String, Object> buildContext(Object target, Method method, Object[] args) {
        // map 上下文
        Map<String, Object> context = new HashMap<>();

        // BeanFactory
        context.put("$$", IOC.getBeanFactory());

        // 方法及参数
        context.put("this", target);
        context.put("m", method);
        context.put("args", args);

        // 详细参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.put("p" + i, parameters[i]);
            if (args != null) {
                context.put("arg" + i, args[i]);
                context.put(parameters[i].getName(), args[i]);
            }
        }

        return context;
    }
}
