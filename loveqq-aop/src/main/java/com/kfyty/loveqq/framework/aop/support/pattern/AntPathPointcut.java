package com.kfyty.loveqq.framework.aop.support.pattern;

import com.kfyty.loveqq.framework.aop.ExpressionPointcut;
import com.kfyty.loveqq.framework.aop.MethodMatcher;

/**
 * 描述: ant 路径切入点
 *
 * @author kfyty725
 * @date 2022/12/1 20:26
 * @email kfyty725@hotmail.com
 */
public class AntPathPointcut implements ExpressionPointcut {
    private final String pattern;

    public AntPathPointcut(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getExpression() {
        return this.pattern;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new AntPathMethodMatcher(this.getExpression());
    }
}
