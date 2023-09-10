package com.kfyty.core.autoconfig.boostrap;

import com.kfyty.core.autoconfig.ConfigurableApplicationContext;

/**
 * 描述: 引导接口，用于微服务自动配置类，在刷新 {@link com.kfyty.core.autoconfig.beans.BeanFactory} 之前直接实例化并执行
 *
 * @author kfyty725
 * @date 2023/9/10 21:34
 * @email kfyty725@hotmail.com
 */
public interface Bootstrap {
    /**
     * 引导启动方法
     */
    void bootstrap(ConfigurableApplicationContext applicationContext) throws Exception;
}
