package com.kfyty.loveqq.framework.aop;

import org.aopalliance.aop.Advice;

/**
 * 描述: Advisor，表示一个完整的切面
 *
 * @author kfyty725
 * @date 2021/7/30 12:29
 * @email kfyty725@hotmail.com
 */
public interface Advisor {
    /**
     * 获取切面通知
     *
     * @return 切面通知
     */
    Advice getAdvice();
}
