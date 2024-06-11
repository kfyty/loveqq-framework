package com.kfyty.loveqq.framework.core.autoconfig.aware;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;

/**
 * 描述: 设置 ConfigurableApplicationContext
 *
 * @author kfyty725
 * @date 2021/6/12 12:08
 * @email kfyty725@hotmail.com
 */
public interface ConfigurableApplicationContextAware {
    void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext);
}
