package com.kfyty.loveqq.framework.core.autoconfig;

/**
 * 描述: bean 实例化后的回调
 *
 * @author kfyty725
 * @date 2021/5/21 16:47
 * @email kfyty725@hotmail.com
 */
public interface InitializingBean {
    /**
     * 初始化 bean
     * <p>
     * 在 {@link BeanPostProcessor#postProcessBeforeDestroy} 后执行
     */
    void afterPropertiesSet();
}
