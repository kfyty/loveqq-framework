package com.kfyty.loveqq.framework.cloud.bootstrap.empty;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.BootstrapConfiguration;

/**
 * 描述: {@link BeanFactory} 引导应用
 *
 * @author kfyty725
 * @date 2023/9/10 22:02
 * @email kfyty725@hotmail.com
 */
@Configuration
@ComponentFilter(value = {"com.kfyty.core.autoconfig.beans.autowired.**" ,"com.kfyty.aop.**", "com.kfyty.loveqq.framework.boot.context.**", "com.kfyty.loveqq.framework.boot.event.**", "com.kfyty.boot.processor.**", "com.kfyty.boot.autoconfig.**"}, annotations = BootstrapConfiguration.class)
public class BeanFactoryBootstrapApplication {
}
