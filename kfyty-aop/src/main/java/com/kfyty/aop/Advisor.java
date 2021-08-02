package com.kfyty.aop;

import org.aopalliance.aop.Advice;

/**
 * 描述: Advisor
 *
 * @author kfyty725
 * @date 2021/7/30 12:29
 * @email kfyty725@hotmail.com
 */
public interface Advisor {
    Advice getAdvice();
}
