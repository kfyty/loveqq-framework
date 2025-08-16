package com.kfyty.loveqq.framework.cloud.bootstrap.internal.empty;

import com.kfyty.loveqq.framework.boot.context.factory.ApplicationContextFactory;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;

/**
 * 描述: 上下文创建工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:31
 * @email kfyty725@hotmail.com
 */
public class BootstrapApplicationContextFactory extends ApplicationContextFactory {

    public ApplicationContext create(String[] commandLineArgs, Class<?> primarySource) {
        ConfigurableApplicationContext configurableApplicationContext = new BootstrapApplicationContext();
        configurableApplicationContext.setCommandLineArgs(commandLineArgs);
        configurableApplicationContext.setPrimarySource(primarySource);
        return configurableApplicationContext;
    }
}
