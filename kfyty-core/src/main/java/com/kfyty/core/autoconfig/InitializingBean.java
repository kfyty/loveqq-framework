package com.kfyty.core.autoconfig;

/**
 * 描述: bean 实例化后的回调
 *
 * @author kfyty725
 * @date 2021/5/21 16:47
 * @email kfyty725@hotmail.com
 */
public interface InitializingBean {
    void afterPropertiesSet();
}
