package com.kfyty.loveqq.framework.core.support;

/**
 * 描述: 路径匹配
 *
 * @author kfyty725
 * @date 2022/11/12 17:29
 * @email kfyty725@hotmail.com
 */
public interface PatternMatcher {
    /**
     * 路径匹配
     *
     * @param pattern 规则，eg: /aa/**
     * @param source  要匹配的字符串，eg: /aa/bb
     * @return true if matched
     */
    boolean matches(String pattern, String source);
}
