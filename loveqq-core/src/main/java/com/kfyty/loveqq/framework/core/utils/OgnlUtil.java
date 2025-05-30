package com.kfyty.loveqq.framework.core.utils;

import ognl.Ognl;

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
        } catch (Exception e) {
            throw new RuntimeException("表达式计算失败: " + e.getMessage(), e);
        }
    }
}
