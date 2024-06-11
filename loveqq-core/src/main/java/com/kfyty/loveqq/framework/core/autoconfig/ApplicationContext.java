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

    Class<?> getPrimarySource();

    ApplicationContext refresh();
}
