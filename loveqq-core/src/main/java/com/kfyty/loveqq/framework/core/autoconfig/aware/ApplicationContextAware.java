package com.kfyty.loveqq.framework.core.autoconfig.aware;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;

/**
 * 描述: 设置 ApplicationContext
 *
 * @author kfyty725
 * @date 2021/6/12 12:08
 * @email kfyty725@hotmail.com
 */
public interface ApplicationContextAware {
    void setApplicationContext(ApplicationContext applicationContext);
}
