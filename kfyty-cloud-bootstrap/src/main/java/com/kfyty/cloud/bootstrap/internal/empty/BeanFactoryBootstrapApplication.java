package com.kfyty.cloud.bootstrap.internal.empty;

import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.boostrap.BootstrapConfiguration;

/**
 * 描述: {@link com.kfyty.core.autoconfig.beans.BeanFactory} 引导应用
 *
 * @author kfyty725
 * @date 2023/9/10 22:02
 * @email kfyty725@hotmail.com
 */
@Configuration
@ComponentFilter(value = {"com.kfyty.aop.**", "com.kfyty.boot.context.**", "com.kfyty.boot.event.**", "com.kfyty.boot.processor.**", "com.kfyty.boot.autoconfig.**"}, annotations = BootstrapConfiguration.class)
public class BeanFactoryBootstrapApplication {
}
