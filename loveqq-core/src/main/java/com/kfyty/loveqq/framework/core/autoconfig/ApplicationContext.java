package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;

/**
 * 描述: 配置上下文
 *
 * @author kfyty725
 * @date 2021/5/21 17:44
 * @email kfyty725@hotmail.com
 */
public interface ApplicationContext extends BeanFactory, ApplicationEventPublisher {
    /**
     * 获取启动类资源
     *
     * @return 启动类资源
     */
    Class<?> getPrimarySource();

    /**
     * 刷新上下文
     *
     * @return 应用上下文
     */
    ApplicationContext refresh();
}
