package com.kfyty.aop;

/**
 * 描述: 表达式切入点
 *
 * @author kfyty725
 * @date 2021/7/30 12:58
 * @email kfyty725@hotmail.com
 */
public interface ExpressionPointcut extends Pointcut {
    String getExpression();
}
