package com.kfyty.loveqq.framework.boot.context.factory;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.boot.context.DefaultConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;

/**
 * 描述: 上下文创建工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:31
 * @email kfyty725@hotmail.com
 */
public class ApplicationContextFactory {

    public ApplicationContext create(K k) {
        return create(k.getCommandLineArgs(), k.getPrimarySource());
    }

    public ApplicationContext create(String[] commandLineArgs, Class<?> primarySource) {
        ConfigurableApplicationContext configurableApplicationContext = new DefaultConfigurableApplicationContext();
        configurableApplicationContext.setCommandLineArgs(commandLineArgs);
        configurableApplicationContext.setPrimarySource(primarySource);
        return configurableApplicationContext;
    }
}
