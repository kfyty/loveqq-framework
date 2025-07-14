package com.kfyty.loveqq.framework.core.autoconfig;

/**
 * 描述: bean 销毁时的回调
 *
 * @author kfyty725
 * @date 2021/5/28 15:50
 * @email kfyty725@hotmail.com
 */
public interface DestroyBean {
    /**
     * 销毁 bean
     * <p>
     * 在 {@link BeanPostProcessor#postProcessBeforeInitialization} 后执行
     */
    void destroy();
}
