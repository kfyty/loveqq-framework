package com.kfyty.loveqq.framework.aop;

/**
 * 描述: 表达式切入点
 *
 * @author kfyty725
 * @date 2021/7/30 12:58
 * @email kfyty725@hotmail.com
 */
public interface ExpressionPointcut extends Pointcut {
    /**
     * 获取切入点表达式
     *
     * @return 切入点表达式
     */
    String getExpression();
}
