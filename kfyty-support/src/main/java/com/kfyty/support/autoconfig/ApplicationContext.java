package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.event.ApplicationEventPublisher;

/**
 * 描述: 配置上下文
 *
 * @author kfyty725
 * @date 2021/5/21 17:44
 * @email kfyty725@hotmail.com
 */
public interface ApplicationContext extends BeanFactory, ApplicationEventPublisher {

    Class<?> getPrimarySource();

    String[] getCommandLineArgs();

    boolean doFilterComponent(Class<?> clazz);

    ApplicationContext refresh();
}
