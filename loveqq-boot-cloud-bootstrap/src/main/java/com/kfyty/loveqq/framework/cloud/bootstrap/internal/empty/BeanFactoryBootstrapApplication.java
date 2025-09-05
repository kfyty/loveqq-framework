package com.kfyty.loveqq.framework.cloud.bootstrap.internal.empty;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.BootstrapConfiguration;

/**
 * 描述: {@link BeanFactory} 引导应用
 * <p>
 * 这里必须指定包名规则，否则会注入三方自动配置，但这些在引导中是不必要的
 * 虽然配置了很多包名，但只有自动配置会被扫描到
 * </p>
 *
 * @author kfyty725
 * @date 2023/9/10 22:02
 * @email kfyty725@hotmail.com
 */
@Component
@ComponentScan(includeFilter = @ComponentFilter(
        annotations = BootstrapConfiguration.class,
        value = {
                "com.kfyty.loveqq.framework.core.autoconfig.**",
                "com.kfyty.loveqq.framework.aop.processor.**",
                "com.kfyty.loveqq.framework.aop.aspectj.creator.**",
                "com.kfyty.loveqq.framework.aop.aspectj.adapter.**",
                "com.kfyty.loveqq.framework.boot.autoconfig.**",
                "com.kfyty.loveqq.framework.boot.context.**",
                "com.kfyty.loveqq.framework.boot.event.**",
                "com.kfyty.loveqq.framework.boot.processor.**"
        }))
public class BeanFactoryBootstrapApplication {
}
