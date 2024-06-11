package com.kfyty.loveqq.framework.core.autoconfig.boostrap;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

/**
 * 描述: 引导接口，用于微服务自动配置类，在刷新 {@link BeanFactory} 之前直接实例化并执行
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
