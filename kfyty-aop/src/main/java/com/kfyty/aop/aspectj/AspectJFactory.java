package com.kfyty.aop.aspectj;

import org.aopalliance.aop.Advice;

/**
 * 描述: aspectJ 切面工厂
 *
 * @author kfyty725
 * @date 2022/11/1 22:23
 * @email kfyty725@hotmail.com
 */
public interface AspectJFactory {
    /**
     * 获取切面实例
     *
     * @param advice 通知
     * @return 实例
     */
    Object getInstance(Advice advice);
}
