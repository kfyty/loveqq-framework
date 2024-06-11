package com.kfyty.loveqq.framework.core.support;

/**
 * 描述: 路径匹配
 *
 * @author kfyty725
 * @date 2022/11/12 17:29
 * @email kfyty725@hotmail.com
 */
public interface PatternMatcher {
    boolean matches(String pattern, String source);
}
