package com.kfyty.support.autoconfig;

import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: bean 定义
 *
 * @author kfyty725
 * @date 2021/5/22 11:13
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
public class BeanDefine {
    private final Class<?> beanType;

    private boolean isInstance;

    public BeanDefine(Class<?> beanType) {
        this.beanType = beanType;
        this.isInstance = false;
    }

    public Object createInstance() {
        Object instance = ReflectUtil.newInstance(this.beanType);
        this.isInstance = true;
        if(log.isDebugEnabled()) {
            log.debug(": instantiate bean: [{}] !", instance);
        }
        return instance;
    }
}
