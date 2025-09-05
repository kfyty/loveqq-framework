package com.kfyty.loveqq.framework.core.autoconfig.aware;

import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;

/**
 * 描述: 设置 {@link com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext}
 *
 * @author kfyty725
 * @date 2021/6/12 12:08
 * @email kfyty725@hotmail.com
 */
public interface PropertyContextAware {
    /**
     * 设置属性配置上下文
     *
     * @param propertiesContext 属性配置上下文
     */
    void setPropertyContext(GenericPropertiesContext propertiesContext);
}
