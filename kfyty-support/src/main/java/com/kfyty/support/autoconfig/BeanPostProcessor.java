package com.kfyty.support.autoconfig;

/**
 * 描述: bean 后置处理器
 *
 * @author fyty
 * @date 2021/5/29 14:29
 * @email kfyty725@hotmail.com
 */
public interface BeanPostProcessor {

    boolean canProcess(Object bean);

    Object postProcess(Object source);
}
